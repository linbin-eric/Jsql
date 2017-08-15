package com.jfireframework.sql;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
import com.jfireframework.sql.dbstructure.Structure;
import com.jfireframework.sql.dbstructure.impl.H2DBStructure;
import com.jfireframework.sql.dbstructure.impl.HsqlDBStructure;
import com.jfireframework.sql.dbstructure.impl.MariaDBStructure;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.mapper.Mapper;
import com.jfireframework.sql.mapper.MapperBuilder;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.page.impl.OracleParse;
import com.jfireframework.sql.page.impl.StandardParse;
import com.jfireframework.sql.resultsettransfer.ResultsetTransferStore;
import com.jfireframework.sql.session.impl.SessionFactoryImpl;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class SessionfactoryConfig
{
    private DataSource                        dataSource;
    private ClassLoader                       classLoader = Thread.currentThread().getContextClassLoader();
    private String                            scanPackage;
    // 如果值是create，则会创建表。
    private String                            tableMode   = "none";
    private ResultsetTransferStore            resultsetTransferStore;
    private Set<Class<?>>                     ckasses;
    private IdentityHashMap<Class<?>, Mapper> mappers     = new IdentityHashMap<Class<?>, Mapper>(128);
    private IdentityHashMap<Class<?>, Dao<?>> daos        = new IdentityHashMap<Class<?>, Dao<?>>();
    private MetaContext                       metaContext;
    private SqlInterceptor[]                  sqlInterceptors;
    private PageParse                         pageParse;
    private String                            productName;
    private JdbcTypeDictionary                jdbcTypeDictionary;
    protected static final Logger             logger      = LoggerFactory.getLogger(SessionfactoryConfig.class);
    
    public SessionFactory build()
    {
        try
        {
            Verify.notNull(dataSource, "no dataSource set");
            Verify.notNull(scanPackage, "sql的扫描路径不能为空");
            resultsetTransferStore = resultsetTransferStore == null ? new ResultsetTransferStore() : resultsetTransferStore;
            Stage[] processors = new Stage[] { //
                    new buildClassSet(), //
                    new initSqlInterceptor(), //
                    new detectProductName(), //
                    new initMetaContext(), //
                    new createOrUpdateDatabase(), //
                    new CreateMappers(), //
                    new BuildDao()//
            };
            for (Stage each : processors)
            {
                each.process();
            }
            return new SessionFactoryImpl(mappers, daos, sqlInterceptors, pageParse, dataSource, resultsetTransferStore);
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
    }
    
    interface Stage
    {
        void process() throws Exception;
    }
    
    class buildClassSet implements Stage
    {
        
        @Override
        public void process() throws Exception
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
        
    }
    
    class initSqlInterceptor implements Stage
    {
        
        @Override
        public void process() throws Exception
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
    }
    
    class detectProductName implements Stage
    {
        
        @Override
        public void process() throws Exception
        {
            Connection connection = null;
            try
            {
                connection = dataSource.getConnection();
                DatabaseMetaData md = connection.getMetaData();
                productName = md.getDatabaseProductName().toLowerCase();
                if (productName.equals("mariadb") || "mysql".equals(productName))
                {
                    pageParse = new StandardParse();
                    jdbcTypeDictionary = new JdbcTypeDictionary.MysqlJdbcTypes();
                }
                else if (productName.equals("oracle"))
                {
                    pageParse = new OracleParse();
                    jdbcTypeDictionary = new JdbcTypeDictionary.MysqlJdbcTypes();
                }
                else if (productName.contains("hsql"))
                {
                    pageParse = new StandardParse();
                    jdbcTypeDictionary = new JdbcTypeDictionary.MysqlJdbcTypes();
                }
                else if (productName.equals("h2"))
                {
                    pageParse = new StandardParse();
                    jdbcTypeDictionary = new JdbcTypeDictionary.MysqlJdbcTypes();
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
        
    }
    
    class initMetaContext implements Stage
    {
        
        @Override
        public void process() throws Exception
        {
            metaContext = new MetaContext(ckasses, jdbcTypeDictionary);
        }
        
    }
    
    class createOrUpdateDatabase implements Stage
    {
        private static final String CREATE = "create";
        private static final String UPDATE = "update";
        private static final String NONE   = "none";
        
        Structure buildStructure()
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
        
        @Override
        public void process() throws Exception
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
        
    }
    
    class CreateMappers implements Stage
    {
        
        @Override
        public void process() throws Exception
        {
            MapperBuilder mapperBuilder = new MapperBuilder(metaContext, resultsetTransferStore, jdbcTypeDictionary);
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
        
    }
    
    class BuildDao implements Stage
    {
        
        @SuppressWarnings({ "rawtypes" })
        @Override
        public void process() throws Exception
        {
            for (TableMetaData each : metaContext.metaDatas())
            {
                if (each.getIdInfo() != null)
                {
                    if (productName.equals("mysql") || productName.equals("marridb"))
                    {
                        daos.put(each.getEntityClass(), new MysqlDAO(each, sqlInterceptors, jdbcTypeDictionary));
                    }
                    else if (productName.equals("oracle"))
                    {
                        daos.put(each.getEntityClass(), new OracleDAO(each, sqlInterceptors, jdbcTypeDictionary));
                    }
                    else if (productName.contains("hsql"))
                    {
                        daos.put(each.getEntityClass(), new StandardDAO(each, sqlInterceptors, jdbcTypeDictionary));
                    }
                    else if (productName.equals("h2"))
                    {
                        daos.put(each.getEntityClass(), new StandardDAO(each, sqlInterceptors, jdbcTypeDictionary));
                    }
                    else
                    {
                        throw new UnsupportedOperationException("不支持的数据库产品");
                    }
                }
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
    
}
