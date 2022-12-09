package com.jfirer.jsql;

import com.jfirer.baseutil.PackageScan;
import com.jfirer.baseutil.TRACEID;
import com.jfirer.baseutil.Verify;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.bytecode.support.SupportOverrideAttributeAnnotationContextFactory;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.jsql.annotation.TableDef;
import com.jfirer.jsql.curd.CurdOpSupport;
import com.jfirer.jsql.curd.impl.OracleCurdOpSupport;
import com.jfirer.jsql.curd.impl.StandardCurdOpSupport;
import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.dialect.impl.H2Dialect;
import com.jfirer.jsql.dialect.impl.MysqlDialect;
import com.jfirer.jsql.dialect.impl.OracleDialect;
import com.jfirer.jsql.executor.SqlExecutor;
import com.jfirer.jsql.executor.impl.FinalExecuteSqlExecutor;
import com.jfirer.jsql.executor.impl.OraclePageExecutor;
import com.jfirer.jsql.executor.impl.StandardPageExecutor;
import com.jfirer.jsql.mapper.AbstractMapper;
import com.jfirer.jsql.mapper.Mapper;
import com.jfirer.jsql.mapper.MapperGenerator;
import com.jfirer.jsql.metadata.TableEntityInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;

public class SessionfactoryConfig
{
    private                DataSource               dataSource;
    private                ClassLoader              classLoader              = Thread.currentThread().getContextClassLoader();
    private                String                   scanPackage;
    private final          List<SqlExecutor>        sqlExecutors             = new LinkedList<SqlExecutor>();
    private                Dialect                  dialect;
    protected static final Logger                   logger                   = LoggerFactory.getLogger(SessionfactoryConfig.class);
    private                AnnotationContextFactory annotationContextFactory = new SupportOverrideAttributeAnnotationContextFactory();

    public SessionFactory build()
    {
        TRACEID.newTraceId();
        try
        {
            Verify.notNull(dataSource, "dataSource 对象不能为空");
            Verify.notNull(scanPackage, "sql的扫描路径不能为空");
            Set<String> classSet    = buildClassSet();
            String      productName = detectProductName();
            dialect = dialect == null ? generateDialect(productName) : dialect;
            return new SessionFactoryImpl(generateMappers(classSet, annotationContextFactory), generateCurdInfos(productName, classSet, annotationContextFactory), generateHeadSqlExecutor(productName), dataSource, dialect);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private IdentityHashMap<Class<?>, Class<? extends AbstractMapper>> generateMappers(Set<String> classSet, AnnotationContextFactory annotationContextFactory)
    {
        Map<String, TableEntityInfo> tableEntityInfos = new HashMap<String, TableEntityInfo>();
        for (String each : classSet)
        {
            AnnotationContext annotationContext = annotationContextFactory.get(each.replace('.', '/'));
            if (annotationContext.isAnnotationPresent(TableDef.class))
            {
                try
                {
                    Class ckass = classLoader.loadClass(each);
                    tableEntityInfos.put(ckass.getSimpleName(), TableEntityInfo.parse(ckass));
                }
                catch (ClassNotFoundException e)
                {
                    ReflectUtil.throwException(e);
                }
            }
        }
        CompileHelper                                              compiler = new CompileHelper(classLoader);
        IdentityHashMap<Class<?>, Class<? extends AbstractMapper>> mappers  = new IdentityHashMap<Class<?>, Class<? extends AbstractMapper>>();
        for (String each : classSet)
        {
            AnnotationContext annotationContext = annotationContextFactory.get(each.replace('.', '/'));
            if (annotationContext.isAnnotationPresent(Mapper.class))
            {
                try
                {
                    Class<?>                        ckass       = classLoader.loadClass(each);
                    Class<? extends AbstractMapper> mapperClass = (Class<? extends AbstractMapper>) MapperGenerator.generate(ckass, tableEntityInfos, compiler);
                    mappers.put(ckass, mapperClass);
                }
                catch (ClassNotFoundException e)
                {
                    ReflectUtil.throwException(e);
                }
            }
        }
        return mappers;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private IdentityHashMap<Class<?>, CurdOpSupport<?>> generateCurdInfos(String productName, Set<String> classSet, AnnotationContextFactory annotationContextFactory)
    {
        IdentityHashMap<Class<?>, CurdOpSupport<?>> curdInfos = new IdentityHashMap<Class<?>, CurdOpSupport<?>>();
        for (String each : classSet)
        {
            if (annotationContextFactory.get(each.replace('.', '/')).isAnnotationPresent(TableDef.class) == false)
            {
                continue;
            }
            try
            {
                Class           ckass           = classLoader.loadClass(each);
                TableEntityInfo tableEntityInfo = TableEntityInfo.parse(ckass);
                if (tableEntityInfo.getPkInfo() == null)
                {
                    continue;
                }
                if ("mysql".equals(productName) || "h2".equalsIgnoreCase(productName))
                {
                    curdInfos.put(ckass, new StandardCurdOpSupport(ckass));
                }
                else if ("oracle".equals(productName))
                {
                    curdInfos.put(ckass, new OracleCurdOpSupport(ckass));
                }
            }
            catch (ClassNotFoundException e)
            {
                ReflectUtil.throwException(e);
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

    private SqlExecutor generateHeadSqlExecutor(String productName)
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
        Optional<SqlExecutor> minOrderExecutor = sqlExecutors.stream()//
                                                             .sorted((e1, e2) -> {
                                                                 int result = e2.order() - e1.order();
                                                                 if (result == 0)
                                                                 {
                                                                     throw new IllegalStateException(e1.getClass().getName() + "和" + e2.getClass().getName() + "的序号重复，这会导致不可预测的结果，请检查");
                                                                 }
                                                                 return result;
                                                             })//
                                                             .reduce((current, next) -> {
                                                                 next.setNext(current);
                                                                 return next;
                                                             });
        return minOrderExecutor.get();
    }

    private Set<String> buildClassSet() throws ClassNotFoundException
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
        return set;
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
