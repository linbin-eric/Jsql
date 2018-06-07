package com.jfireframework.sql;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.PackageScan;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.smc.compiler.JavaStringCompiler;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.curd.CurdInfo;
import com.jfireframework.sql.curd.impl.MysqlCurdInfo;
import com.jfireframework.sql.curd.impl.OracleCurdInfo;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.dialect.impl.H2Dialect;
import com.jfireframework.sql.dialect.impl.MysqlDialect;
import com.jfireframework.sql.dialect.impl.OracleDialect;
import com.jfireframework.sql.executor.SqlExecutor;
import com.jfireframework.sql.executor.SqlInvoker;
import com.jfireframework.sql.executor.impl.DefaultSqlExecutor;
import com.jfireframework.sql.executor.impl.MysqlPageExecutor;
import com.jfireframework.sql.executor.impl.OraclePageExecutor;
import com.jfireframework.sql.mapper.Mapper;
import com.jfireframework.sql.mapper.MapperGenerator;
import com.jfireframework.sql.session.impl.SessionFactoryImpl;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;
import com.jfireframework.sql.util.TableEntityInfo;
import com.jfireframework.sql.util.TableMode;

public class SessionfactoryConfig
{
	private DataSource				dataSource;
	private ClassLoader				classLoader		= Thread.currentThread().getContextClassLoader();
	private String					scanPackage;
	// 如果值是create，则会创建表。
	private TableMode				tableMode		= TableMode.NONE;
	private List<SqlExecutor>		sqlExecutors	= new LinkedList<SqlExecutor>();
	private Dialect					dialect;
	protected static final Logger	logger			= LoggerFactory.getLogger(SessionfactoryConfig.class);
	
	public SessionFactory build()
	{
		TRACEID.newTraceId();
		try
		{
			Verify.notNull(dataSource, "dataSource 对象不能为空");
			Verify.notNull(scanPackage, "sql的扫描路径不能为空");
			String productName = detectProductName();
			dialect = dialect == null ? generateDialect(productName) : dialect;
			Set<Class<?>> classSet = buildClassSet();
			return new SessionFactoryImpl(generateMappers(classSet), generateCurdInfos(productName, classSet), generateHeadSqlInvoker(productName), dataSource, dialect);
		}
		catch (Exception e)
		{
			throw new JustThrowException(e);
		}
	}
	
	private IdentityHashMap<Class<?>, Mapper> generateMappers(Set<Class<?>> classSet) throws InstantiationException, IllegalAccessException
	{
		Map<String, TableEntityInfo> tableEntityInfos = new HashMap<String, TableEntityInfo>();
		for (Class<?> each : classSet)
		{
			if (each.isAnnotationPresent(TableEntity.class))
			{
				tableEntityInfos.put(each.getSimpleName(), TableEntityInfo.parse(each));
			}
		}
		JavaStringCompiler compiler = new JavaStringCompiler(classLoader);
		IdentityHashMap<Class<?>, Mapper> mappers = new IdentityHashMap<Class<?>, Mapper>();
		for (Class<?> each : classSet)
		{
			if (each.isInterface())
			{
				boolean find = false;
				for (Method method : each.getMethods())
				{
					if (method.isAnnotationPresent(Sql.class))
					{
						find = true;
						break;
					}
				}
				if (find)
				{
					Mapper mapper = (Mapper) MapperGenerator.generate(each, tableEntityInfos, compiler).newInstance();
					mappers.put(each, mapper);
				}
			}
		}
		return mappers;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private IdentityHashMap<Class<?>, CurdInfo<?>> generateCurdInfos(String productName, Set<Class<?>> classSet)
	{
		IdentityHashMap<Class<?>, CurdInfo<?>> curdInfos = new IdentityHashMap<Class<?>, CurdInfo<?>>();
		for (Class<?> each : classSet)
		{
			if (each.isAnnotationPresent(TableEntity.class) == false)
			{
				continue;
			}
			TableEntityInfo tableEntityInfo = TableEntityInfo.parse(each);
			if (tableEntityInfo.getPkField() == null)
			{
				continue;
			}
			if ("mysql".equals(productName) || "h2".equalsIgnoreCase(productName))
			{
				curdInfos.put(each, new MysqlCurdInfo(each));
			}
			else if ("oracle".equals(productName))
			{
				curdInfos.put(each, new OracleCurdInfo(each));
			}
		}
		return curdInfos;
	}
	
	private Dialect generateDialect(String productName)
	{
		if (productName.equals("mariadb") || "mysql".equals(productName))
		{
			return new MysqlDialect();
		}
		else if (productName.equals("oracle"))
		{
			return new OracleDialect();
		}
		else if (productName.equals("h2"))
		{
			return new H2Dialect();
		}
		else
		{
			throw new UnsupportedOperationException("不识别的数据库类型" + productName);
		}
	}
	
	private SqlInvoker generateHeadSqlInvoker(String productName)
	{
		sqlExecutors.add(new DefaultSqlExecutor());
		if ("mysql".equalsIgnoreCase(productName))
		{
			sqlExecutors.add(new MysqlPageExecutor());
		}
		else if ("oracle".equalsIgnoreCase(productName))
		{
			sqlExecutors.add(new OraclePageExecutor());
		}
		Collections.sort(sqlExecutors, new Comparator<SqlExecutor>() {
			
			@Override
			public int compare(SqlExecutor o1, SqlExecutor o2)
			{
				return o1.order() - o2.order();
			}
		});
		SqlInvoker pred = new SqlInvoker() {
			
			@Override
			public int update(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException
			{
				throw new UnsupportedOperationException();
			}
			
			@Override
			public Object queryOne(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer) throws SQLException
			{
				throw new UnsupportedOperationException();
			}
			
			@Override
			public List<Object> queryList(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer) throws SQLException
			{
				throw new UnsupportedOperationException();
			}
			
			@Override
			public String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException
			{
				throw new UnsupportedOperationException();
			}
		};
		int index = sqlExecutors.size() - 1;
		for (int i = index; i > -1; i--)
		{
			final SqlExecutor sqlExecutor = sqlExecutors.get(i);
			final SqlInvoker next = pred;
			SqlInvoker sqlInvoker = new SqlInvoker() {
				
				@Override
				public int update(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException
				{
					return sqlExecutor.update(sql, params, connection, dialect, next);
				}
				
				@Override
				public Object queryOne(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer) throws SQLException
				{
					return sqlExecutor.queryOne(sql, params, connection, dialect, resultSetTransfer, next);
				}
				
				@Override
				public List<Object> queryList(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer) throws SQLException
				{
					return sqlExecutor.queryList(sql, params, connection, dialect, resultSetTransfer, next);
				}
				
				@Override
				public String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException
				{
					return sqlExecutor.insertWithReturnKey(sql, params, connection, dialect, next);
				}
			};
			pred = sqlInvoker;
		}
		SqlInvoker head = pred;
		return head;
	}
	
	private Set<Class<?>> buildClassSet() throws ClassNotFoundException
	{
		Set<String> set = new HashSet<String>();
		String[] packageNames = scanPackage.split(";");
		for (String packageName : packageNames)
		{
			for (String each : PackageScan.scan(packageName))
			{
				set.add(each);
			}
		}
		Set<Class<?>> types = new HashSet<Class<?>>();
		for (String each : set)
		{
			types.add(classLoader.loadClass(each));
		}
		return types;
	}
	
	private String detectProductName() throws SQLException
	{
		Connection connection = null;
		try
		{
			connection = dataSource.getConnection();
			DatabaseMetaData md = connection.getMetaData();
			return md.getDatabaseProductName().toLowerCase();
		}
		finally
		{
			if (connection != null)
			{
				connection.close();
			}
		}
	}
	
	public void setTableMode(TableMode tableMode)
	{
		this.tableMode = tableMode;
	}
	
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}
	
	public void setClassLoader(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}
	
	public void setScanPackage(String scanPackage)
	{
		this.scanPackage = scanPackage;
	}
	
	public Dialect getDialect()
	{
		return dialect;
	}
	
	public void setDialect(Dialect dialect)
	{
		this.dialect = dialect;
	}
	
}
