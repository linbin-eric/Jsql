package com.jfireframework.sql.session.impl;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import com.jfireframework.baseutil.PackageScan;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.simplelog.ConsoleLogFactory;
import com.jfireframework.baseutil.simplelog.Logger;
import com.jfireframework.baseutil.uniqueid.SummerId;
import com.jfireframework.baseutil.uniqueid.Uid;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.dao.Dao;
import com.jfireframework.sql.dao.impl.MysqlDAO;
import com.jfireframework.sql.dao.impl.OracleDAO;
import com.jfireframework.sql.dao.impl.StandardDAO;
import com.jfireframework.sql.dbstructure.H2DBStructure;
import com.jfireframework.sql.dbstructure.HsqlDBStructure;
import com.jfireframework.sql.dbstructure.MariaDBStructure;
import com.jfireframework.sql.dbstructure.Structure;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.interceptor.SqlPreInterceptor;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.page.OracleParse;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.page.StandardParse;
import com.jfireframework.sql.session.SessionFactory;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.session.mapper.Mapper;
import com.jfireframework.sql.util.MapperBuilder;

public abstract class SessionFactoryBootstrap implements SessionFactory
{
    @Resource
    protected DataSource                        dataSource;
    @Resource
    protected ClassLoader                       classLoader;
    protected static ThreadLocal<SqlSession>    sessionLocal = new ThreadLocal<SqlSession>();
    protected String                            scanPackage;
    // 如果值是create，则会创建表。
    protected String                            tableMode    = "none";
    protected IdentityHashMap<Class<?>, Mapper> mappers      = new IdentityHashMap<Class<?>, Mapper>(128);
    protected IdentityHashMap<Class<?>, Dao<?>> daos         = new IdentityHashMap<Class<?>, Dao<?>>();
    protected MetaContext                       metaContext;
    protected SqlPreInterceptor[]               preInterceptors;
    protected SqlInterceptor[]                  sqlInterceptors;
    protected PageParse                         pageParse;
    protected String                            productName;
    protected static final Logger               logger       = ConsoleLogFactory.getLogger();
    protected int                               workerid     = 0;
    protected Uid                               uid;
    
    @PostConstruct
    public void init()
    {
        try
        {
            if (dataSource == null)
            {
                return;
            }
            if (classLoader == null)
            {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            uid = new SummerId(workerid);
            Set<Class<?>> set = buildClassNameSet(classLoader);
            preInterceptors = findSqlPreInterceptors(set);
            sqlInterceptors = findSqlInterceptor(set);
            pageParse = findPageParse();
            metaContext = new MetaContext(set);
            createOrUpdateDatabase();
            createMappers(set);
            new DaoBuilder().buildDao();
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
    }
    
    public void setScanPackage(String scanPackage)
    {
        this.scanPackage = scanPackage;
    }
    
    private SqlPreInterceptor[] findSqlPreInterceptors(Set<Class<?>> set) throws InstantiationException, IllegalAccessException
    {
        List<SqlPreInterceptor> list = new LinkedList<SqlPreInterceptor>();
        for (Class<?> each : set)
        {
            if (SqlPreInterceptor.class.isAssignableFrom(each))
            {
                list.add((SqlPreInterceptor) each.newInstance());
            }
        }
        return list.toArray(new SqlPreInterceptor[list.size()]);
    }
    
    private SqlInterceptor[] findSqlInterceptor(Set<Class<?>> set) throws InstantiationException, IllegalAccessException
    {
        List<SqlInterceptor> list = new LinkedList<SqlInterceptor>();
        for (Class<?> each : set)
        {
            if (SqlInterceptor.class.isAssignableFrom(each))
            {
                list.add((SqlInterceptor) each.newInstance());
            }
        }
        return list.toArray(new SqlInterceptor[list.size()]);
    }
    
    public PageParse findPageParse()
    {
        Connection connection = null;
        try
        {
            connection = dataSource.getConnection();
            DatabaseMetaData md = connection.getMetaData();
            productName = md.getDatabaseProductName().toLowerCase();
            if (productName.equals("mariadb") || "mysql".equals(productName))
            {
                return new StandardParse();
            }
            else if (productName.equals("oracle"))
            {
                return new OracleParse();
            }
            else if (productName.contains("hsql"))
            {
                return new StandardParse();
            }
            else if (productName.equals("h2"))
            {
                return new StandardParse();
            }
            else
            {
                logger.error("不支持分页的数据库类型：{}", productName);
                return null;
            }
        }
        catch (SQLException e)
        {
            throw new JustThrowException(e);
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e)
                {
                    throw new JustThrowException(e);
                }
            }
        }
    }
    
    private Set<Class<?>> buildClassNameSet(ClassLoader classLoader) throws ClassNotFoundException
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
    
    private void createMappers(Set<Class<?>> set)
    {
        try
        {
            
            MapperBuilder mapperBuilder = new MapperBuilder(metaContext);
            nextSqlInterface: for (Class<?> each : set)
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
            for (Mapper each : mappers.values())
            {
                each.setSessionFactory(this);
            }
        }
        catch (Exception e1)
        {
            throw new JustThrowException(e1);
        }
    }
    
    private Structure buildStructure()
    {
        if (productName.equals("mysql"))
        {
            return new MariaDBStructure();
        }
        else if (productName.equals("mariadb"))
        {
            return new MariaDBStructure();
        }
        else if (productName.contains("hsql"))
        {
            return new HsqlDBStructure();
        }
        else if (productName.equals("h2"))
        {
            return new H2DBStructure();
        }
        else
        {
            throw new IllegalArgumentException("暂不支持" + productName + "数据库结构类型新建或者修改,当前支持：mysql,MariaDB");
        }
    }
    
    enum TableMode
    {
        create, update, none
    }
    
    private void createOrUpdateDatabase() throws Exception
    {
        TableMode type = TableMode.valueOf(tableMode);
        switch (type)
        {
            case none:
                return;
            case create:
            {
                Structure structure = buildStructure();
                structure.createTable(dataSource, metaContext.metaDatas());
                return;
            }
            case update:
            {
                Structure structure = buildStructure();
                structure.updateTable(dataSource, metaContext.metaDatas());
                return;
            }
        }
    }
    
    class DaoBuilder
    {
        @SuppressWarnings("rawtypes")
        public void buildDao()
        {
            for (TableMetaData each : metaContext.metaDatas())
            {
                if (each.getIdInfo() != null)
                {
                    if (productName.equals("mysql") || productName.equals("marridb"))
                    {
                        daos.put(each.getEntityClass(), new MysqlDAO(each, preInterceptors, uid));
                    }
                    else if (productName.equals("oracle"))
                    {
                        daos.put(each.getEntityClass(), new OracleDAO(each, preInterceptors, uid));
                    }
                    else if (productName.contains("hsql"))
                    {
                        daos.put(each.getEntityClass(), new StandardDAO(each, preInterceptors, uid));
                    }
                    else if (productName.equals("h2"))
                    {
                        daos.put(each.getEntityClass(), new StandardDAO(each, preInterceptors, uid));
                    }
                    else
                    {
                        throw new UnsupportedOperationException("不支持的数据库产品");
                    }
                }
            }
        }
        
    }
}
