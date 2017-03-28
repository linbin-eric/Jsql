package com.jfireframework.sql.test;

import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.session.impl.SessionFactoryImpl;
import com.jfireframework.sql.test.findstrategy.UserStrategy;
import com.zaxxer.hikari.HikariDataSource;

public class H2Test
{
    SessionFactoryImpl sessionFactory;
    
    @Before
    public void before()
    {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test");
        // dataSource.setJdbcUrl("jdbc:oracle:thin:@192.168.10.21:1521/orcl");
        // dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("SA");
        dataSource.setPassword("");
        // dataSource.setUsername("test8");
        // dataSource.setPassword("bs");
        dataSource.setMaximumPoolSize(150);
        dataSource.setConnectionTimeout(1500);
        sessionFactory = new SessionFactoryImpl(dataSource);
        sessionFactory.setScanPackage("com.jfireframework.sql.test.findstrategy");
        sessionFactory.setTableMode("create");
        sessionFactory.init();
        buildData();
    }
    
    private void buildData()
    {
        SqlSession session = sessionFactory.openSession();
        session.beginTransAction(0);
        for (int i = 1; i < 10; i++)
        {
            UserStrategy user = new UserStrategy();
            user.setAge(i);
            user.setBirthday("2016-10-0" + i);
            user.setBoy(i % 2 == 0);
            user.setName("test-" + i);
            user.setPassword("pass-" + i);
            session.insert(user);
        }
        session.commit();
        session.close();
    }
    
    @Test
    public void test()
    {
        SqlSession session = sessionFactory.openSession();
        UserStrategy user = new UserStrategy();
        user.setAge(5);
        user.setBoy(false);
        UserStrategy result = session.findOneByStrategy(user, "name,password;age,boy");
        Assert.assertEquals("test-5", result.getName());
        Assert.assertEquals("pass-5", result.getPassword());
        Assert.assertNull(user.getBirthday());
        user = new UserStrategy();
        user.setId(5);
        result = session.findOneByStrategy(user, "name,age,birthday;id");
        Assert.assertNull(result.getPassword());
        Assert.assertEquals("2016-10-05", result.getBirthday());
    }
    
    @Test
    public void test1()
    {
        SqlSession session = sessionFactory.openSession();
        UserStrategy user = new UserStrategy();
        user.setBoy(false);
        List<UserStrategy> result = session.findAllByStrategy(user, "id,password,age;boy");
        for (int i = 1; i <= 5; i++)
        {
            UserStrategy one = result.get(i - 1);
            Assert.assertEquals("pass-" + (i * 2 - 1), one.getPassword());
            Assert.assertEquals(i * 2 - 1, one.getId().intValue());
            Assert.assertNull(one.getBirthday());
            Assert.assertEquals(i * 2 - 1, one.getAge().intValue());
        }
    }
    
    @Test
    public void test2()
    {
        Page page = new Page();
        page.setPage(1);
        page.setPageSize(2);
        SqlSession session = sessionFactory.openSession();
        UserStrategy user = new UserStrategy();
        user.setBoy(false);
        List<UserStrategy> result = session.findPageByStrategy(user, page, "id,password,age;boy");
        for (int i = 1; i <= 2; i++)
        {
            UserStrategy one = result.get(i - 1);
            Assert.assertEquals("pass-" + (i * 2 - 1), one.getPassword());
            Assert.assertEquals(i * 2 - 1, one.getId().intValue());
            Assert.assertNull(one.getBirthday());
            Assert.assertEquals(i * 2 - 1, one.getAge().intValue());
        }
        Assert.assertEquals(2, page.getData().size());
        Assert.assertEquals(5, page.getTotal());
    }
    
    @Test
    public void test3()
    {
        SqlSession session = sessionFactory.openSession();
        UserStrategy user = new UserStrategy();
        user.setAge(5);
        user.setPassword("tttt");
        session.updateByStrategy(user, "password,age;age");
        UserStrategy query = session.get(UserStrategy.class, 5);
        Assert.assertEquals("tttt", query.getPassword());
    }
}
