package com.jfirer.jsql.test;

import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.SessionfactoryConfig;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.model.ModelFactory;
import com.jfirer.jsql.model.Param;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.test.vo.SqlLog;
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
import static org.junit.Assert.assertEquals;
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
        config.addSqlExecutor(new SqlLog());
        sessionFactory = config.build();
        SqlSession sqlSession = sessionFactory.openSession();
        sqlSession.execute("DROP TABLE IF EXISTS user", new LinkedList<>());
        sqlSession.execute("DROP TABLE IF EXISTS user2", new LinkedList<>());
        sqlSession.execute(userTableDml, new LinkedList<>());
        sqlSession.execute(user2TableDml, new LinkedList<>());
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
        session.execute(ModelFactory.deleteFrom(User.class).where(Param.eq(User::getName, "1221").and(Param.eq(User::getAge, 12))));
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
        User query = session.findOne(ModelFactory.selectAll().from(User.class).where(Param.eq(User::getAge, 12)));
        Assert.assertNotNull(query);
        Assert.assertEquals("ll", query.getName());
        query = session.findOne(ModelFactory.select(User::getName, User::getAge).from(User.class).where(Param.eq(User::getAge, 12)));
        Assert.assertNotNull(query);
        Assert.assertEquals("ll", query.getName());
        Assert.assertEquals(12, query.getAge());
        user.setId(null);
        user.setAge(19);
        user.setName("ll");
        session.save(user);
        List<User> list = session.findList(ModelFactory.selectAll().from(User.class).where(Param.eq(User::getName, "ll")));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(12 + 19, list.get(0).getAge() + list.get(1).getAge());
        Assert.assertEquals(2, session.count(ModelFactory.selectCount().from(User.class).where(Param.eq(User::getName, "ll"))));
        Page page = new Page();
        page.setOffset(0);
        page.setSize(1);
        page.setFetchSum(true);
        list = session.findList(ModelFactory.select(User::getAge).from(User.class).where(Param.eq(User::getName, "ll")).page(page));
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
        session.execute(ModelFactory.update(User.class).set(User::getAge, 12).where(Param.eq(User::getId, 1)));
        User query = session.findOne(ModelFactory.selectAll().from(User.class).where(Param.eq(User::getId,1)));
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
        List<User> result = session.findList(ModelFactory.select(User::getName).from(User.class).orderBy(User::getAge, true));
        Assert.assertEquals("aa2", result.get(0).getName());
        Assert.assertEquals("aa1", result.get(1).getName());
    }

    @Test
    public void test_4()
    {
        SqlSession session = sessionFactory.openSession();
        int        age     = new Random().nextInt(150);
        session.execute(ModelFactory.insert(User.class).insert(User::getName, "aa1").insert(User::getAge, age));
        User user = session.findOne(ModelFactory.selectAll().from(User.class).where(Param.eq(User::getName, "aa1").and(Param.eq(User::getAge, age))));
        assertNotNull(user);
    }


}
