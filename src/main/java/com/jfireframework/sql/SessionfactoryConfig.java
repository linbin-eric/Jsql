package com.jfireframework.sql;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.PackageScan;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.order.AescComparator;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.dao.Dao;
import com.jfireframework.sql.dao.impl.H2DAO;
import com.jfireframework.sql.dao.impl.MysqlDAO;
import com.jfireframework.sql.dao.impl.OracleDAO;
import com.jfireframework.sql.dbstructure.Structure;
import com.jfireframework.sql.dbstructure.column.ColumnTypeDictionary;
import com.jfireframework.sql.dbstructure.column.impl.H2ColumnTypeDictionary;
import com.jfireframework.sql.dbstructure.column.impl.MysqlColumnTypeDictionary;
import com.jfireframework.sql.dbstructure.column.impl.OracleColumnTypeDictionary;
import com.jfireframework.sql.dbstructure.impl.H2DBStructure;
import com.jfireframework.sql.dbstructure.impl.MysqlDBStructure;
import com.jfireframework.sql.dbstructure.impl.OracleStructure;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.dialect.impl.H2Dialect;
import com.jfireframework.sql.dialect.impl.MysqlDialect;
import com.jfireframework.sql.dialect.impl.OracleDialect;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.mapfield.FieldOperatorDictionary;
import com.jfireframework.sql.mapper.Mapper;
import com.jfireframework.sql.mapper.MapperBuilder;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.page.impl.MysqlPage;
import com.jfireframework.sql.page.impl.OracleParse;
import com.jfireframework.sql.resultsettransfer.ResultSetTransferDictionary;
import com.jfireframework.sql.resultsettransfer.ResultsetTransferStore;
import com.jfireframework.sql.session.impl.SessionFactoryImpl;
import com.jfireframework.sql.util.TableNameCaseStrategy;

public class SessionfactoryConfig
{
	
	private static final String					CREATE					= "create";
	private static final String					UPDATE					= "update";
	private static final String					NONE					= "none";
	protected static final Logger				logger					= LoggerFactory.getLogger(SessionfactoryConfig.class);
	private DataSource							dataSource;
	private ClassLoader							classLoader				= Thread.currentThread().getContextClassLoader();
	private String								scanPackage;
	private String								schema;
	// 如果值是create，则会创建表。
	private String								tableMode				= "none";
	private ResultsetTransferStore				resultsetTransferStore;
	private Set<Class<?>>						ckasses;
	private IdentityHashMap<Class<?>, Mapper>	mappers					= new IdentityHashMap<Class<?>, Mapper>(128);
	private IdentityHashMap<Class<?>, Dao>		daos					= new IdentityHashMap<Class<?>, Dao>();
	private MetaContext							metaContext;
	private SqlInterceptor[]					sqlInterceptors;
	private PageParse							pageParse;
	private String								productName;
	private ColumnTypeDictionary				columnTypeDictionary;
	private FieldOperatorDictionary				fieldOperatorDictionary;
	private ResultSetTransferDictionary			resultSetTransferDictionary;
	private Dialect								dialect;
	private TableNameCaseStrategy				tableNameCaseStrategy	= TableNameCaseStrategy.ORIGIN;
	
	public SessionFactory build()
	{
		TRACEID.newTraceId();
		try
		{
			Verify.notNull(schema, "schema 不能为空");
			Verify.notNull(dataSource, "dataSource 对象不能为空");
			Verify.notNull(scanPackage, "sql的扫描路径不能为空");
			resultSetTransferDictionary = resultSetTransferDictionary == null ? new ResultSetTransferDictionary.BuildInResultSetTransferDictionary() : resultSetTransferDictionary;
			fieldOperatorDictionary = fieldOperatorDictionary == null ? new FieldOperatorDictionary.BuildInFieldOperatorDictionary() : fieldOperatorDictionary;
			resultsetTransferStore = new ResultsetTransferStore(resultSetTransferDictionary, SessionfactoryConfig.this);
			buildClassSet();
			initSqlInterceptor();
			detectProductName();
			initMetaContext();
			createOrUpdateDatabase();
			createMappers();
			buildDao();
			return new SessionFactoryImpl(mappers, daos, sqlInterceptors, pageParse, dataSource, resultsetTransferStore, dialect);
		}
		catch (Exception e)
		{
			throw new JustThrowException(e);
		}
	}
	
	private void createMappers() throws InstantiationException, IllegalAccessException
	{
		MapperBuilder mapperBuilder = new MapperBuilder(metaContext, resultsetTransferStore);
		nextSqlInterface: for (Class<?> each : ckasses)
		{
			if (each.isInterface())
			{
				for (Method method : each.getMethods())
				{
					if (method.isAnnotationPresent(Sql.class))
					{
						mappers.put(each, (Mapper) mapperBuilder.build(each).newInstance());
						continue nextSqlInterface;
					}
				}
			}
		}
	}
	
	private void createOrUpdateDatabase() throws SQLException
	{
		if (NONE.equals(tableMode))
		{
			return;
		}
		else if (CREATE.equals(tableMode))
		{
			Structure structure = buildStructure();
			structure.createTable(dataSource, metaContext.metaDatas());
		}
		else if (UPDATE.equals(tableMode))
		{
			Structure structure = buildStructure();
			structure.updateTable(dataSource, metaContext.metaDatas());
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private void initMetaContext()
	{
		metaContext = new MetaContext(ckasses, SessionfactoryConfig.this);
	}
	
	private void buildClassSet() throws ClassNotFoundException
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
		ckasses = types;
	}
	
	private void initSqlInterceptor() throws InstantiationException, IllegalAccessException
	{
		List<SqlInterceptor> list = new LinkedList<SqlInterceptor>();
		for (Class<?> each : ckasses)
		{
			if (SqlInterceptor.class.isAssignableFrom(each) && each.isInterface() == false)
			{
				list.add((SqlInterceptor) each.newInstance());
			}
		}
		Collections.sort(list, new AescComparator());
		sqlInterceptors = list.toArray(new SqlInterceptor[list.size()]);
	}
	
	private void detectProductName() throws SQLException
	{
		Connection connection = null;
		try
		{
			connection = dataSource.getConnection();
			DatabaseMetaData md = connection.getMetaData();
			productName = md.getDatabaseProductName().toLowerCase();
			if (productName.equals("mariadb") || "mysql".equals(productName))
			{
				pageParse = new MysqlPage();
				columnTypeDictionary = columnTypeDictionary == null ? new MysqlColumnTypeDictionary() : columnTypeDictionary;
				dialect = new MysqlDialect();
			}
			else if (productName.equals("oracle"))
			{
				pageParse = new OracleParse();
				columnTypeDictionary = columnTypeDictionary == null ? new OracleColumnTypeDictionary() : columnTypeDictionary;
				dialect = new OracleDialect();
			}
			else if (productName.equals("h2"))
			{
				pageParse = new MysqlPage();
				columnTypeDictionary = columnTypeDictionary == null ? new H2ColumnTypeDictionary() : columnTypeDictionary;
				dialect = new H2Dialect();
			}
			else
			{
				logger.error("不支持分页的数据库类型：{}", productName);
			}
		}
		finally
		{
			if (connection != null)
			{
				connection.close();
			}
		}
	}
	
	Structure buildStructure()
	{
		if (productName.equals("mysql"))
		{
			return new MysqlDBStructure(schema);
		}
		else if (productName.equals("mariadb"))
		{
			return new MysqlDBStructure(schema);
		}
		else if (productName.equals("h2"))
		{
			return new H2DBStructure(schema);
		}
		else if (productName.equals("oracle"))
		{
			return new OracleStructure(schema);
		}
		else
		{
			throw new IllegalArgumentException("暂不支持" + productName + "数据库结构类型新建或者修改,当前支持：mysql,MariaDB,Oracle");
		}
	}
	
	private void buildDao()
	{
		for (TableMetaData each : metaContext.metaDatas())
		{
			if (each.getIdInfo() != null)
			{
				Dao dao;
				if (productName.equals("mysql") || productName.equals("marridb"))
				{
					dao = new MysqlDAO();
				}
				else if (productName.equals("oracle"))
				{
					dao = new OracleDAO();
				}
				else if (productName.equals("h2"))
				{
					dao = new H2DAO();
				}
				else
				{
					throw new UnsupportedOperationException("不支持的数据库产品");
				}
				dao.initialize(each, sqlInterceptors, SessionfactoryConfig.this, pageParse, dialect);
				daos.put(each.getEntityClass(), dao);
			}
		}
	}
	
	public void setTableMode(String tableMode)
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
	
	public ColumnTypeDictionary getColumnTypeDictionary()
	{
		return columnTypeDictionary;
	}
	
	public void setColumnTypeDictionary(ColumnTypeDictionary columnTypeDictionary)
	{
		this.columnTypeDictionary = columnTypeDictionary;
	}
	
	public FieldOperatorDictionary getFieldOperatorDictionary()
	{
		return fieldOperatorDictionary;
	}
	
	public void setFieldOperatorDictionary(FieldOperatorDictionary fieldOperatorDictionary)
	{
		this.fieldOperatorDictionary = fieldOperatorDictionary;
	}
	
	public ResultSetTransferDictionary getResultSetTransferDictionary()
	{
		return resultSetTransferDictionary;
	}
	
	public void setResultSetTransferDictionary(ResultSetTransferDictionary resultSetTransferDictionary)
	{
		this.resultSetTransferDictionary = resultSetTransferDictionary;
	}
	
	public String getSchema()
	{
		return schema;
	}
	
	public void setSchema(String schema)
	{
		this.schema = schema;
	}
	
	public MetaContext getMetaContext()
	{
		return metaContext;
	}
	
	public void setTableNameCaseStrategy(TableNameCaseStrategy tableNameCaseStrategy)
	{
		this.tableNameCaseStrategy = tableNameCaseStrategy;
	}
	
	public TableNameCaseStrategy getTableNameCaseStrategy()
	{
		return tableNameCaseStrategy;
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
