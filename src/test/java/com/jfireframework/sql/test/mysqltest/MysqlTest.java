package com.jfireframework.sql.test.mysqltest;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Test;
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.SqlSession;
import com.mysql.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;

public class MysqlTest
{
    @Test
    public void test()
    {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306");
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUsername("root");
        dataSource.setPassword("centerm");
        SessionfactoryConfig config = new SessionfactoryConfig();
        config.setDataSource(dataSource);
        config.setScanPackage("com.jfireframework.sql.test.mysqltest");
        config.setSchema("test");
        config.setTableMode("update");
        SessionFactory sessionFactory = config.build();
        SqlSession session = sessionFactory.openSession();
    }
    
    @Test
    public void test2() throws SQLException
    {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306");
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUsername("root");
        dataSource.setPassword("centerm");
        Connection connection = dataSource.getConnection();
        connection.prepareStatement("insert into test.test_demo (ID) values(NULL)").executeUpdate();
    }
}
