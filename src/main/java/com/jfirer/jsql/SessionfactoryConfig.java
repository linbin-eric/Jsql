package com.jfirer.jsql;

import com.jfirer.jsql.annotation.Sql;
import com.jfirer.jsql.annotation.TableDef;
import com.jfirer.jsql.curd.CurdInfo;
import com.jfirer.jsql.curd.impl.OracleCurdInfo;
import com.jfirer.jsql.curd.impl.StandardCurdInfo;
import com.jfirer.jsql.dbstructure.impl.H2SchemaAdjustment;
import com.jfirer.jsql.dbstructure.impl.MysqlSchemaAdjustment;
import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.dialect.impl.H2Dialect;
import com.jfirer.jsql.dialect.impl.MysqlDialect;
import com.jfirer.jsql.dialect.impl.OracleDialect;
import com.jfirer.jsql.executor.FinalExecuteSqlExecutor;
import com.jfirer.jsql.executor.SqlExecutor;
import com.jfirer.jsql.executor.SqlInvoker;
import com.jfirer.jsql.executor.impl.OraclePageExecutor;
import com.jfirer.jsql.executor.impl.StandardPageExecutor;
import com.jfirer.jsql.mapper.AbstractMapper;
import com.jfirer.jsql.mapper.Mapper;
import com.jfirer.jsql.mapper.MapperGenerator;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.metadata.TableMode;
import com.jfirer.jsql.transfer.resultset.ResultSetTransfer;
import com.jfirer.baseutil.PackageScan;
import com.jfirer.baseutil.TRACEID;
import com.jfirer.baseutil.Verify;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;

public class SessionfactoryConfig
{
    private                DataSource        dataSource;
    private                ClassLoader       classLoader  = Thread.currentThread().getContextClassLoader();
    private                String            scanPackage;
    // 如果值是create，则会创建表。
    private                TableMode         tableMode    = TableMode.NONE;
    private final          List<SqlExecutor> sqlExecutors = new LinkedList<SqlExecutor>();
    private                Dialect           dialect;
    protected static final Logger            logger       = LoggerFactory.getLogger(SessionfactoryConfig.class);

    public SessionFactory build()
    {
        TRACEID.newTraceId();
        try
        {
            Verify.notNull(dataSource, "dataSource 对象不能为空");
            Verify.notNull(scanPackage, "sql的扫描路径不能为空");
            Set<Class<?>> classSet    = buildClassSet();
            String        productName = detectProductName();
            modifySchema(classSet, productName);
            dialect = dialect == null ? generateDialect(productName) : dialect;
            return new SessionFactoryImpl(generateMappers(classSet), generateCurdInfos(productName, classSet), generateHeadSqlInvoker(productName), dataSource, dialect);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    private void modifySchema(Set<Class<?>> classSet, String productName) throws SQLException
    {
        Set<TableEntityInfo> tableEntityInfos = new HashSet<TableEntityInfo>();
        for (Class<?> ckass : classSet)
        {
            if (ckass.isAnnotationPresent(TableDef.class) && ckass.getAnnotation(TableDef.class).editable())
            {
                tableEntityInfos.add(TableEntityInfo.parse(ckass));
            }
        }
        if ("mysql".equals(productName))
        {
            new MysqlSchemaAdjustment().adjust(tableMode, dataSource, tableEntityInfos);
        }
        else if ("h2".equalsIgnoreCase(productName))
        {
            new H2SchemaAdjustment().adjust(tableMode, dataSource, tableEntityInfos);
        }
    }

    @SuppressWarnings("unchecked")
    private IdentityHashMap<Class<?>, Class<? extends AbstractMapper>> generateMappers(Set<Class<?>> classSet)
    {
        Map<String, TableEntityInfo> tableEntityInfos = new HashMap<String, TableEntityInfo>();
        for (Class<?> each : classSet)
        {
            if (each.isAnnotationPresent(TableDef.class))
            {
                tableEntityInfos.put(each.getSimpleName(), TableEntityInfo.parse(each));
            }
        }
        CompileHelper                                              compiler = new CompileHelper(classLoader);
        IdentityHashMap<Class<?>, Class<? extends AbstractMapper>> mappers  = new IdentityHashMap<Class<?>, Class<? extends AbstractMapper>>();
        for (Class<?> each : classSet)
        {
            if (each.isInterface() && each.isAnnotationPresent(Mapper.class))
            {
                Class<? extends AbstractMapper> mapperClass = (Class<? extends AbstractMapper>) MapperGenerator.generate(each, tableEntityInfos, compiler);
                mappers.put(each, mapperClass);
            }
        }
        return mappers;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private IdentityHashMap<Class<?>, CurdInfo<?>> generateCurdInfos(String productName, Set<Class<?>> classSet)
    {
        IdentityHashMap<Class<?>, CurdInfo<?>> curdInfos = new IdentityHashMap<Class<?>, CurdInfo<?>>();
        for (Class<?> each : classSet)
        {
            if (each.isAnnotationPresent(TableDef.class) == false)
            {
                continue;
            }
            TableEntityInfo tableEntityInfo = TableEntityInfo.parse(each);
            if (tableEntityInfo.getPkInfo() == null)
            {
                continue;
            }
            if ("mysql".equals(productName) || "h2".equalsIgnoreCase(productName))
            {
                curdInfos.put(each, new StandardCurdInfo(each));
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
        if ("mysql".equalsIgnoreCase(productName) || "h2".equalsIgnoreCase(productName))
        {
            sqlExecutors.add(new StandardPageExecutor());
        }
        else if ("oracle".equalsIgnoreCase(productName))
        {
            sqlExecutors.add(new OraclePageExecutor());
        }
        sqlExecutors.add(new FinalExecuteSqlExecutor());
        Collections.sort(sqlExecutors, new Comparator<SqlExecutor>()
        {

            @Override
            public int compare(SqlExecutor o1, SqlExecutor o2)
            {
                int result = o1.order() - o2.order();
                if (result == 0)
                {
                    throw new IllegalStateException(o1.getClass().getName() + "和" + o2.getClass().getName() + "的序号重复，这会导致不可预测的结果，请检查");
                }
                return result;
            }
        });
        SqlInvoker pred  = null;
        int        index = sqlExecutors.size() - 1;
        for (int i = index; i > -1; i--)
        {
            final SqlExecutor sqlExecutor = sqlExecutors.get(i);
            final SqlInvoker  next        = pred;
            pred = new SqlInvoker()
            {

                @Override
                public int update(String sql, List<Object> params, Connection connection, Dialect dialect1) throws SQLException
                {
                    return sqlExecutor.update(sql, params, connection, dialect1, next);
                }

                @Override
                public Object queryOne(String sql, List<Object> params, Connection connection, Dialect dialect1, ResultSetTransfer resultSetTransfer) throws SQLException
                {
                    return sqlExecutor.queryOne(sql, params, connection, dialect1, resultSetTransfer, next);
                }

                @Override
                public List<Object> queryList(String sql, List<Object> params, Connection connection, Dialect dialect1, ResultSetTransfer resultSetTransfer) throws SQLException
                {
                    return sqlExecutor.queryList(sql, params, connection, dialect1, resultSetTransfer, next);
                }

                @Override
                public String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect1) throws SQLException
                {
                    return sqlExecutor.insertWithReturnKey(sql, params, connection, dialect1, next);
                }
            };
        }
        return pred;
    }

    private Set<Class<?>> buildClassSet() throws ClassNotFoundException
    {
        Set<String> set          = new HashSet<String>();
        String[]    packageNames = scanPackage.split(";");
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

    public void addSqlExecutor(SqlExecutor sqlExecutor)
    {
        sqlExecutors.add(sqlExecutor);
    }
}
