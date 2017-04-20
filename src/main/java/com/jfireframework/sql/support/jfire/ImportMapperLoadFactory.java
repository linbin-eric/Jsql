package com.jfireframework.sql.support.jfire;

import javax.annotation.Resource;
import javax.sql.DataSource;
import com.jfireframework.jfire.config.annotation.Bean;
import com.jfireframework.jfire.config.annotation.Configuration;
import com.jfireframework.jfire.config.environment.Environment;
import com.jfireframework.sql.session.SessionFactory;

@Configuration
public class ImportMapperLoadFactory
{
    @Resource
    private Environment environment;
    @Resource
    private DataSource  dataSource;
    @Resource
    private ClassLoader classLoader;
    
    @Bean
    public SessionFactory sessionFactory()
    {
        EnableJfireSql enableJfireSql = environment.getAnnotation(EnableJfireSql.class);
        MapperLoadFactory factory = new MapperLoadFactory();
        factory.setScanPackage(enableJfireSql.scanPackage());
        factory.setTableMode(enableJfireSql.tableMode());
        factory.setDataSource(dataSource);
        factory.setClassLoader(classLoader);
        factory.init();
        return factory;
    }
}
