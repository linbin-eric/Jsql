package com.jfirer.jsql.test;

import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.SessionFactoryConfig;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.Param;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.test.vo.*;
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
        SessionFactoryConfig config     = new SessionFactoryConfig();
        HikariDataSource     dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:orderdb;mode=mysql");
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
        int count = session.count(Model.selectCount(User3::getName).from(User3.class, "user2").innerJoin(User2.class, "user").on(Param.eq(User3::getAge, User2::getAge)));
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
        List<String> result = session.findList(Model.select(User::getName).from(User.class).orderBy(User::getAge, true));
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
        User one = session.findOne(Model.selectAll(User.class).where(Param.notNull(User::getName)));
        assertNotNull(one);
        assertEquals(10, one.getAge());
    }

    @Test
    public void test_11()
    {
        SqlSession session = sessionFactory.openSession();
        User2      user2   = new User2();
        user2.setName("A");
        user2.setAge(14);
        session.save(user2);
        User3 user3 = new User3();
        user3.setName("B");
        user3.setAge(14);
        session.save(user3);
        List<UserDTO> list = session.findList(Model.selectAlias(User2::getName,"name2").from(User2.class, "u").leftJoin(User3.class, "user3").on(Param.eq(User2::getAge, User3::getAge))//
                                                   .where(Param.eq(User2::getId, 1))//
                                                   .returnType(UserDTO.class));
        assertEquals("A", list.get(0).getName2());
    }

    /**
     * 测试带boolean参数的条件方法 - eq
     */
    @Test
    public void test_conditionalEq()
    {
        SqlSession session = sessionFactory.openSession();
        User user = new User();
        user.setName("testUser");
        user.setAge(25);
        session.save(user);

        // condition为true时应该添加条件
        User result = session.findOne(Model.selectAll(User.class).where(Param.eq(true, User::getName, "testUser")));
        assertNotNull(result);
        assertEquals("testUser", result.getName());

        // condition为false时不应该添加条件，查询所有记录
        result = session.findOne(Model.selectAll(User.class).where(Param.eq(false, User::getName, "nonExistent")));
        assertNotNull(result); // 因为false时条件被忽略，应该能查到记录
    }

    /**
     * 测试带boolean参数的条件方法 - notNull和isNull
     */
    @Test
    public void test_conditionalNull()
    {
        SqlSession session = sessionFactory.openSession();
        User user = new User();
        user.setName("testUser");
        user.setAge(25);
        session.save(user);

        // condition为true时应该添加notNull条件
        User result = session.findOne(Model.selectAll(User.class).where(Param.notNull(true, User::getName)));
        assertNotNull(result);

        // condition为false时不添加条件
        result = session.findOne(Model.selectAll(User.class).where(Param.notNull(false, User::getName)));
        assertNotNull(result);
    }

    /**
     * 测试带boolean参数的比较运算符 - bt, lt, be, le
     */
    @Test
    public void test_conditionalComparison()
    {
        SqlSession session = sessionFactory.openSession();
        User user1 = new User();
        user1.setName("user1");
        user1.setAge(20);
        session.save(user1);

        User user2 = new User();
        user2.setName("user2");
        user2.setAge(30);
        session.save(user2);

        // condition为true时应该添加大于条件
        List<User> result = session.findList(Model.selectAll(User.class).where(Param.bt(true, User::getAge, 25)));
        assertEquals(1, result.size());
        assertEquals("user2", result.get(0).getName());

        // condition为false时不添加条件，应该查到所有记录
        result = session.findList(Model.selectAll(User.class).where(Param.bt(false, User::getAge, 100)));
        assertEquals(2, result.size());

        // 测试小于等于
        result = session.findList(Model.selectAll(User.class).where(Param.le(true, User::getAge, 20)));
        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getName());
    }

    /**
     * 测试带boolean参数的between方法
     */
    @Test
    public void test_conditionalBetween()
    {
        SqlSession session = sessionFactory.openSession();
        User user1 = new User();
        user1.setName("user1");
        user1.setAge(20);
        session.save(user1);

        User user2 = new User();
        user2.setName("user2");
        user2.setAge(30);
        session.save(user2);

        User user3 = new User();
        user3.setName("user3");
        user3.setAge(40);
        session.save(user3);

        // condition为true时应该添加between条件
        List<User> result = session.findList(Model.selectAll(User.class).where(Param.between(true, User::getAge, 25, 35)));
        assertEquals(1, result.size());
        assertEquals("user2", result.get(0).getName());

        // condition为false时不添加条件
        result = session.findList(Model.selectAll(User.class).where(Param.between(false, User::getAge, 1, 10)));
        assertEquals(3, result.size());
    }

    /**
     * 测试带boolean参数的字符串模式匹配 - startWith, endWith, contain, like
     */
    @Test
    public void test_conditionalStringPattern()
    {
        SqlSession session = sessionFactory.openSession();
        User user1 = new User();
        user1.setName("testUser");
        user1.setAge(20);
        session.save(user1);

        User user2 = new User();
        user2.setName("anotherUser");
        user2.setAge(30);
        session.save(user2);

        // condition为true时应该添加startWith条件
        List<User> result = session.findList(Model.selectAll(User.class).where(Param.startWith(true, User::getName, "test")));
        assertEquals(1, result.size());
        assertEquals("testUser", result.get(0).getName());

        // condition为false时不添加条件
        result = session.findList(Model.selectAll(User.class).where(Param.startWith(false, User::getName, "xyz")));
        assertEquals(2, result.size());

        // 测试contain
        result = session.findList(Model.selectAll(User.class).where(Param.contain(true, User::getName, "User")));
        assertEquals(2, result.size());

        // 测试endWith
        result = session.findList(Model.selectAll(User.class).where(Param.endWith(true, User::getName, "User")));
        assertEquals(2, result.size());
    }

    /**
     * 测试带boolean参数的in方法
     */
    @Test
    public void test_conditionalIn()
    {
        SqlSession session = sessionFactory.openSession();
        User user1 = new User();
        user1.setName("user1");
        user1.setAge(20);
        session.save(user1);

        User user2 = new User();
        user2.setName("user2");
        user2.setAge(30);
        session.save(user2);

        User user3 = new User();
        user3.setName("user3");
        user3.setAge(40);
        session.save(user3);

        // condition为true时应该添加in条件
        List<User> result = session.findList(Model.selectAll(User.class).where(Param.in(true, User::getAge, 20, 30)));
        assertEquals(2, result.size());

        // condition为false时不添加条件
        result = session.findList(Model.selectAll(User.class).where(Param.in(false, User::getAge, 999)));
        assertEquals(3, result.size());

        // 测试String类型的in
        result = session.findList(Model.selectAll(User.class).where(Param.in(true, User::getName, "user1", "user3")));
        assertEquals(2, result.size());
    }

    /**
     * 测试带boolean参数的notIn方法
     */
    @Test
    public void test_conditionalNotIn()
    {
        SqlSession session = sessionFactory.openSession();
        User user1 = new User();
        user1.setName("user1");
        user1.setAge(20);
        session.save(user1);

        User user2 = new User();
        user2.setName("user2");
        user2.setAge(30);
        session.save(user2);

        User user3 = new User();
        user3.setName("user3");
        user3.setAge(40);
        session.save(user3);

        // condition为true时应该添加notIn条件
        List<User> result = session.findList(Model.selectAll(User.class).where(Param.notIn(true, User::getAge, 20, 30)));
        assertEquals(1, result.size());
        assertEquals("user3", result.get(0).getName());

        // condition为false时不添加条件
        result = session.findList(Model.selectAll(User.class).where(Param.notIn(false, User::getAge, 20, 30, 40)));
        assertEquals(3, result.size());
    }

    /**
     * 测试组合多个带boolean参数的条件
     */
    @Test
    public void test_conditionalCombination()
    {
        SqlSession session = sessionFactory.openSession();
        User user1 = new User();
        user1.setName("alice");
        user1.setAge(20);
        session.save(user1);

        User user2 = new User();
        user2.setName("bob");
        user2.setAge(30);
        session.save(user2);

        User user3 = new User();
        user3.setName("charlie");
        user3.setAge(40);
        session.save(user3);

        boolean includeAgeFilter = true;
        boolean includeNameFilter = false;

        // 只有年龄条件生效
        List<User> result = session.findList(Model.selectAll(User.class)
                .where(Param.bt(includeAgeFilter, User::getAge, 25)
                        .and(Param.startWith(includeNameFilter, User::getName, "x"))));
        assertEquals(2, result.size()); // bob和charlie的年龄都大于25

        // 两个条件都生效
        includeNameFilter = true;
        result = session.findList(Model.selectAll(User.class)
                .where(Param.bt(includeAgeFilter, User::getAge, 25)
                        .and(Param.startWith(includeNameFilter, User::getName, "c"))));
        assertEquals(1, result.size());
        assertEquals("charlie", result.get(0).getName());

        // 两个条件都不生效
        includeAgeFilter = false;
        includeNameFilter = false;
        result = session.findList(Model.selectAll(User.class)
                .where(Param.bt(includeAgeFilter, User::getAge, 100)
                        .and(Param.startWith(includeNameFilter, User::getName, "xyz"))));
        assertEquals(3, result.size()); // 没有任何过滤条件，返回所有记录
    }

    @Test
    public void testBitwise(){
        SqlSession session = sessionFactory.openSession();
        User       user    = new User();
        user.setName("1221");
        user.setAge(12);
        session.save(user);
        User one = session.findOne(Model.select(User::getId).where(Param.bitwiseAndByEquals(User::getAge, 4, 4)));
        assertEquals(one.getId(), user.getId());
    }
}
