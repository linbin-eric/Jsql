package com.jfireframework.sql.test;

import java.sql.SQLException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.session.SessionFactory;
import com.jfireframework.sql.session.impl.SessionFactoryBootstrap;
import com.jfireframework.sql.session.impl.SessionFactoryImpl;
import com.jfireframework.sql.test.oracle.User12;
import com.jfireframework.sql.test.oracle.UserOp;
import com.zaxxer.hikari.HikariDataSource;
import oracle.net.aso.p;

public class OracleTest
{
    private SessionFactory sessionFactory;
    
    @Before
    public void before()
    {
        HikariDataSource dataSource = new HikariDataSource();
        // dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/test?characterEncoding=utf8");
        dataSource.setJdbcUrl("jdbc:oracle:thin:@192.168.10.21:1521/orcl");
        dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        // dataSource.setConnectionTestQuery("select count(*) from dual");
        // dataSource.setUsername("root");
        // dataSource.setPassword("centerm");
        dataSource.setUsername("test8");
        dataSource.setPassword("bs");
        dataSource.setMaximumPoolSize(150);
        dataSource.setConnectionTimeout(1500);
        try
        {
            dataSource.getConnection();
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sessionFactory = new SessionFactoryImpl(dataSource);
        ((SessionFactoryBootstrap) sessionFactory).setScanPackage("com.jfireframework.sql.test.oracle");
        ((SessionFactoryBootstrap) sessionFactory).init();
    }
    
    @Test
    public void test()
    {
        sessionFactory.getOrCreateCurrentSession();
        UserOp userOp = sessionFactory.getMapper(UserOp.class);
        Page page = new Page();
        page.setPage(1);
        page.setPageSize(3);
        userOp.find(page);
        Assert.assertEquals(3, page.getData().size());
        Assert.assertEquals(13, page.getTotal());
    }
}
