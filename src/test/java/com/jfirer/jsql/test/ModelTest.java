package com.jfirer.jsql.test;

import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.SessionfactoryConfig;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.metadata.TableMode;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.test.vo.User;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.Driver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static com.jfirer.jsql.test.CURDTest.user2TableDml;
import static com.jfirer.jsql.test.CURDTest.userTableDml;
import static org.junit.Assert.assertNotNull;

public class ModelTest
{
    private SessionFactory sessionFactory;

    @Before
    public void before()
    {
        SessionfactoryConfig config     = new SessionfactoryConfig();
        HikariDataSource     dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:orderdb");
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        config.setDataSource(dataSource);
        config.setClassLoader(ModelTest.class.getClassLoader());
        config.setScanPackage(User.class.getPackage().getName());
        sessionFactory = config.build();
        SqlSession sqlSession = sessionFactory.openSession();
        sqlSession.update("DROP TABLE IF EXISTS user", new LinkedList<>());
        sqlSession.update("DROP TABLE IF EXISTS user2", new LinkedList<>());
        sqlSession.update(userTableDml, new LinkedList<>());
        sqlSession.update(user2TableDml, new LinkedList<>());
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
        User       user    = new User();
        user.setName("1221");
        user.setAge(12);
        session.save(user);
        ResultSet resultSet = session.getConnection().prepareStatement("select age from user where name2 ='1221'").executeQuery();
        Assert.assertTrue(resultSet.next());
        Assert.assertEquals(12, resultSet.getInt(1));
        session.delete(Model.delete(User.class).where("name", "1221").where("age", 12));
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
        User query = session.findOne(Model.query(User.class).where("age", 12));
        Assert.assertNotNull(query);
        Assert.assertEquals("ll", query.getName());
        query = session.findOne(Model.query(User.class).select("name").select("age").where("age", 12));
        Assert.assertNotNull(query);
        Assert.assertEquals("ll", query.getName());
        Assert.assertEquals(12, query.getAge());
        user.setId(null);
        user.setAge(19);
        user.setName("ll");
        session.save(user);
        List<User> list = session.find(Model.query(User.class).where("name", "ll"));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(12 + 19, list.get(0).getAge() + list.get(1).getAge());
        Assert.assertEquals(2, session.count(Model.count(User.class).where("name", "ll")));
        Page page = new Page();
        page.setOffset(0);
        page.setSize(1);
        page.setFetchSum(true);
        list = session.find(Model.query(User.class).select("age").where("name", "ll").setPage(page));
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(2, page.getTotal());
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
        session.update(Model.update(User.class).set("age", 12).where("id", 1));
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
        List<User> result = session.find(Model.query(User.class).select("name").orderBy("age", true));
        Assert.assertEquals("aa2", result.get(0).getName());
        Assert.assertEquals("aa1", result.get(1).getName());
    }

    @Test
    public void test_4()
    {
        SqlSession session = sessionFactory.openSession();
        int        age     = new Random().nextInt(150);
        session.insert(Model.insert(User.class).insert("name", "aa1").insert("age", age));
        User user = session.findOne(Model.query(User.class).where("name", "aa1").where("age", age));
        assertNotNull(user);
    }
}
