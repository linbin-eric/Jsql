package com.jfirer.jsql.test;

import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.SessionfactoryConfig;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.Param;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.test.vo.SqlLog;
import com.jfirer.jsql.test.vo.User;
import com.jfirer.jsql.test.vo.User2;
import com.jfirer.jsql.test.vo.User3;
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
import static org.junit.Assert.*;

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
        session.execute(Model.deleteFrom(User.class).where(Param.eq(User::getName, "1221").and(Param.eq(User::getAge, 12))));
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
        User query = session.findOne(Model.selectAll().from(User.class).where(Param.eq(User::getAge, 12)));
        Assert.assertNotNull(query);
        Assert.assertEquals("ll", query.getName());
        query = session.findOne(Model.select(User::getName, User::getAge).from(User.class).where(Param.eq(User::getAge, 12)));
        Assert.assertNotNull(query);
        Assert.assertEquals("ll", query.getName());
        Assert.assertEquals(12, query.getAge());
        user.setId(null);
        user.setAge(19);
        user.setName("ll");
        session.save(user);
        List<User> list = session.findList(Model.selectAll().from(User.class).where(Param.eq(User::getName, "ll")));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(12 + 19, list.get(0).getAge() + list.get(1).getAge());
        Assert.assertEquals(2, session.count(Model.selectCount(User.class).where(Param.eq(User::getName, "ll"))));
        Page page = new Page();
        page.setOffset(0);
        page.setSize(1);
        page.setFetchSum(true);
        list = session.findList(Model.select(User::getAge).from(User.class).where(Param.eq(User::getName, "ll")).page(page));
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
        session.execute(Model.update(User.class).set(User::getAge, 12).where(Param.eq(User::getId, 1)));
        User query = session.findOne(Model.selectAll().from(User.class).where(Param.eq(User::getId, 1)));
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
        List<User> result = session.findList(Model.select(User::getName).returnType(User.class).orderBy(User::getAge, true));
        Assert.assertEquals("aa2", result.get(0).getName());
        Assert.assertEquals("aa1", result.get(1).getName());
    }

    @Test
    public void test_4()
    {
        SqlSession session = sessionFactory.openSession();
        int        age     = new Random().nextInt(150);
        session.execute(Model.insert(User.class).insert(User::getName, "aa1").insert(User::getAge, age));
        User user = session.findOne(Model.selectAll().from(User.class).where(Param.eq(User::getName, "aa1").and(Param.eq(User::getAge, age))));
        assertNotNull(user);
    }

    /**
     * 测试 join
     */
    @Test
    public void test_5()
    {
        SqlSession session = sessionFactory.openSession();
        User2      user2   = new User2();
        user2.setId(1);
        user2.setName("user1");
        user2.setAge(15);
        session.insert(user2);
        User3 user3 = new User3();
        user3.setId("1");
        user3.setName("user2");
        user3.setAge(15);
        session.insert(user3);
        user3.setId("2");
        user3.setAge(20);
        session.insert(user3);
        int count = session.count(Model.selectCount(User3::getName).from(User3.class).innerJoin(User2.class).on(Param.eq(User3::getAge, User2::getAge)));
        Assert.assertEquals(1, count);
    }

    /**
     * 测试函数方法
     */
    @Test
    public void test_6()
    {
        SqlSession session = sessionFactory.openSession();
        User2      user2   = new User2();
        user2.setId(1);
        user2.setName("user1");
        user2.setAge(15);
        session.insert(user2);
        user2.setId(3);
        session.insert(user2);
        int maxId = session.findOne(Model.selectWithFunction(User2::getId, "max", null).from(User2.class).returnType(Integer.class));
        assertEquals(3, maxId);
    }

    /**
     * 测试返回类型自动获取
     */
    @Test
    public void test_7()
    {
        SqlSession session = sessionFactory.openSession();
        User       user    = new User();
        user.setName("aa1");
        user.setAge(10);
        session.save(user);
        List<String> result = session.findList(Model.select(User::getName).orderBy(User::getAge, true));
        Assert.assertEquals("aa1", result.get(0));
    }

    @Test
    public void test_8()
    {
        SqlSession session = sessionFactory.openSession();
        User       user    = new User();
        user.setName("aa1");
        user.setAge(10);
        session.save(user);
        List<User> list = session.findList(Model.selectAll(User.class).exclude(User::getAge));
        assertEquals("aa1", list.get(0).getName());
        assertEquals(0, list.get(0).getAge());
    }

    @Test
    public void test_9()
    {
        SqlSession session = sessionFactory.openSession();
        User       user    = new User();
        user.setName("aa1");
        user.setAge(10);
        session.save(user);
        User xxx = session.findOne(Model.selectAll(User.class).where(Param.eq(User::getAge, 10).ifAnd(() -> true, Param.eq(User::getName, "xxx"))));
        assertNull(xxx);
        xxx = session.findOne(Model.selectAll(User.class).where(Param.eq(User::getAge, 12).ifOr(() -> true, Param.eq(User::getName, "aa1"))));
        assertEquals(10, xxx.getAge());
    }

    @Test
    public void test_10()
    {
        SqlSession session = sessionFactory.openSession();
        User       user    = new User();
        user.setName("aa1");
        user.setAge(10);
        session.save(user);
        User one = session.findOne(Model.selectAll(User.class).where(Param.notEq(User::getName, null)));
        assertNotNull(one);
        assertEquals(10, one.getAge());
    }
}
