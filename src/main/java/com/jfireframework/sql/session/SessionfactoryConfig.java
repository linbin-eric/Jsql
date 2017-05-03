package com.jfireframework.sql.session;

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
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.order.AescComparator;
import com.jfireframework.baseutil.verify.Verify;
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
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.page.OracleParse;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.page.StandardParse;
import com.jfireframework.sql.session.impl.SessionFactoryImpl;
import com.jfireframework.sql.session.mapper.Mapper;
import com.jfireframework.sql.util.MapperBuilder;

public class SessionfactoryConfig
{
    private DataSource                        dataSource;
    private ClassLoader                       classLoader = Thread.currentThread().getContextClassLoader();
    private String                            scanPackage;
    // 如果值是create，则会创建表。
    private String                            tableMode   = "none";
    private IdentityHashMap<Class<?>, Mapper> mappers     = new IdentityHashMap<Class<?>, Mapper>(128);
    private IdentityHashMap<Class<?>, Dao<?>> daos        = new IdentityHashMap<Class<?>, Dao<?>>();
    private MetaContext                       metaContext;
    private SqlInterceptor[]                  sqlInterceptors;
    private PageParse                         pageParse;
    private String                            productName;
    protected static final Logger             logger      = LoggerFactory.getLogger(SessionfactoryConfig.class);
    
    public SessionFactory build()
    {
        try
        {
            if (dataSource == null)
            {
                throw new NullPointerException("no dataSource set");
            }
            Verify.notNull(scanPackage, "sql的扫描路径不能为空");
            Set<Class<?>> set = buildClassNameSet(classLoader);
            initSqlInterceptor(set);
            detectProductName();
            buildPageParse();
            initMetaContext(set);
            createOrUpdateDatabase();
            createMappers(set);
            buildDao();
            return new SessionFactoryImpl(mappers, daos, sqlInterceptors, pageParse, dataSource);
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
    }
    
    private void buildDao()
    {
        new DaoBuilder().buildDao();
    }
    
    private void initMetaContext(Set<Class<?>> set)
    {
        try
        {
            metaContext = new MetaContext(set);
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
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
    
    private void initSqlInterceptor(Set<Class<?>> set) throws InstantiationException, IllegalAccessException
    {
        List<SqlInterceptor> list = new LinkedList<SqlInterceptor>();
        for (Class<?> each : set)
        {
            if (SqlInterceptor.class.isAssignableFrom(each) && each.isInterface() == false)
            {
                list.add((SqlInterceptor) each.newInstance());
            }
        }
        Collections.sort(list, new AescComparator());
        sqlInterceptors = list.toArray(new SqlInterceptor[list.size()]);
    }
    
    private void detectProductName()
    {
        Connection connection = null;
        try
        {
            connection = dataSource.getConnection();
            DatabaseMetaData md = connection.getMetaData();
            productName = md.getDatabaseProductName().toLowerCase();
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
    
    private void buildPageParse()
    {
        if (productName.equals("mariadb") || "mysql".equals(productName))
        {
            pageParse = new StandardParse();
        }
        else if (productName.equals("oracle"))
        {
            pageParse = new OracleParse();
        }
        else if (productName.contains("hsql"))
        {
            pageParse = new StandardParse();
        }
        else if (productName.equals("h2"))
        {
            pageParse = new StandardParse();
        }
        else
        {
            logger.error("不支持分页的数据库类型：{}", productName);
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
                        daos.put(each.getEntityClass(), new MysqlDAO(each));
                    }
                    else if (productName.equals("oracle"))
                    {
                        daos.put(each.getEntityClass(), new OracleDAO(each));
                    }
                    else if (productName.contains("hsql"))
                    {
                        daos.put(each.getEntityClass(), new StandardDAO(each));
                    }
                    else if (productName.equals("h2"))
                    {
                        daos.put(each.getEntityClass(), new StandardDAO(each));
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
