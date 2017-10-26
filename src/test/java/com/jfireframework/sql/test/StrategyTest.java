package com.jfireframework.sql.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.h2.Driver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.SqlSession;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.test.vo.User;
import com.jfireframework.sql.util.TableNameCaseStrategy;
import com.zaxxer.hikari.HikariDataSource;

public class StrategyTest
{
    private SessionFactory sessionFactory;
    
    @Before
    public void before()
    {
        SessionfactoryConfig config = new SessionfactoryConfig();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:orderdb");
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        config.setDataSource(dataSource);
        config.setSchema("PUBLIC");
        config.setTableNameCaseStrategy(TableNameCaseStrategy.UPPER);
        config.setClassLoader(StrategyTest.class.getClassLoader());
        config.setTableMode("create");
        config.setScanPackage(User.class.getPackage().getName());
        sessionFactory = config.build();
    }
    
    /**
     * 测试策略删除
     * 
     * @throws SQLException
     */
    @Test
    public void test() throws SQLException
    {
        SqlSession session = sessionFactory.openSession();
        User user = new User();
        user.setName("1221");
        user.setAge(12);
        session.save(user);
        ResultSet resultSet = session.getConnection().prepareStatement("select age from user where name2 ='1221'").executeQuery();
        Assert.assertTrue(resultSet.next());
        Assert.assertEquals(12, resultSet.getInt(1));
        session.delete(User.class, "name,age", "1221", 12);
        resultSet = session.getConnection().prepareStatement("select count(*) from user").executeQuery();
        resultSet.next();
        Assert.assertEquals(0, resultSet.getInt(1));
    }
    
    /**
     * 测试策略获取
     */
    @Test
    public void test_1()
    {
        User user = new User();
        user.setName("aa1");
        user.setAge(10);
        SqlSession session = sessionFactory.openSession();
        session.save(user);
        user.setId(null);
        user.setAge(12);
        user.setName("ll");
        session.save(user);
        User query = session.findOne(User.class, "*;age", 12);
        Assert.assertNotNull(query);
        Assert.assertEquals("ll", query.getName());
        query = session.findOne(User.class, "name;age", 12);
        Assert.assertNotNull(query);
        Assert.assertEquals("ll", query.getName());
        Assert.assertEquals(12, query.getAge());
        user.setId(null);
        user.setAge(19);
        user.setName("ll");
        session.save(user);
        List<User> list = session.findAll(User.class, "*;name", "ll");
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(12 + 19, list.get(0).getAge() + list.get(1).getAge());
        Assert.assertEquals(2, session.count(User.class, "name", "ll"));
        Page page = new Page();
        page.setOffset(0);
        page.setSize(1);
        page.setFetchSum(true);
        list = session.findPage(User.class, page, "age;name", "ll");
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(2, page.getTotal());
        sessionFactory.cleanAllData();
        Assert.assertEquals(0, session.count(User.class, ""));
    }
    
    /**
     * 测试策略更新
     */
    @Test
    public void test_2()
    {
        User user = new User();
        user.setName("aa1");
        user.setAge(10);
        SqlSession session = sessionFactory.openSession();
        session.save(user);
        session.update(User.class, "age;id", 12, 1);
        User query = session.get(User.class, 1);
        Assert.assertEquals(12, query.getAge());
    }
    
    /**
     * 测试策略获取中的排序功能
     */
    @Test
    public void test_3()
    {
        User user = new User();
        user.setName("aa1");
        user.setAge(10);
        SqlSession session = sessionFactory.openSession();
        session.save(user);
        user.setId(null);
        user.setAge(12);
        user.setName("aa2");
        session.save(user);
        List<User> result = session.findAll(User.class, "name;;age:desc");
        Assert.assertEquals("aa2", result.get(0).getName());
        Assert.assertEquals("aa1", result.get(1).getName());
    }
}
