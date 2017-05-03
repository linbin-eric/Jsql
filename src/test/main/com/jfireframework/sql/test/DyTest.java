package com.jfireframework.sql.test;

import org.h2.Driver;
import org.junit.Test;
import com.jfireframework.sql.session.SessionFactory;
import com.jfireframework.sql.session.SessionfactoryConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DyTest
{
    @Test
    public void test()
    {
        SessionfactoryConfig config = new SessionfactoryConfig();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:orderdb");
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        config.setDataSource(dataSource);
        config.setClassLoader(DyTest.class.getClassLoader());
        config.setScanPackage(DyTest.class.getPackage().getName());
        config.setTableMode("create");
        SessionFactory sessionFactory = config.build();
    }
}
