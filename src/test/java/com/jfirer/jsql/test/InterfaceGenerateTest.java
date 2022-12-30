package com.jfirer.jsql.test;

import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.SessionfactoryConfig;
import com.jfirer.jsql.annotation.Sql;
import com.jfirer.jsql.mapper.Mapper;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.test.vo.User;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.Driver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.jfirer.jsql.test.CURDTest.user2TableDml;
import static com.jfirer.jsql.test.CURDTest.userTableDml;

public class InterfaceGenerateTest
{
    private SessionfactoryConfig config;

    @Before
    public void before() throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        config = new SessionfactoryConfig();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:orderdb");
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        config.setDataSource(dataSource);
        Set<Class<?>> set = new HashSet<Class<?>>();
        set.add(User.class);
    }

    protected void build(Class<?> ckass)
    {
        SessionFactory sessionFactory = config.build();
        SqlSession     session        = sessionFactory.openSession();
        SqlSession     sqlSession     = sessionFactory.openSession();
        sqlSession.execute("DROP TABLE IF EXISTS user", new LinkedList<>());
        sqlSession.execute("DROP TABLE IF EXISTS user2", new LinkedList<>());
        sqlSession.execute(userTableDml, new LinkedList<>());
        sqlSession.execute(user2TableDml, new LinkedList<>());
        session.getMapper(ckass);
    }

    /**
     *
     */
    @Test
    public void test_4()
    {
        build(test_4.class);
    }

    /**
     * 测试as
     */
    @Test
    public void test_1()
    {
        build(test_1.class);
    }

    /**
     * 测试缺少</if>的情况
     */
    @Test
    public void test_10()
    {
        try
        {
            build(test_10.class);
            Assert.fail();
        }
        catch (Exception ignored)
        {
        }
    }

    /**
     * 测试静态属性
     */
    @Test
    public void test_2()
    {
        build(test_2.class);
    }

    @Mapper
    public interface test_1
    {
        @Sql(sql = "select * from User as u where u.name= ${T(com.jfirer.jsql.test.vo.User).xx}", paramNames = "")
        List<User> find3();
    }

    @Mapper
    public interface test_2
    {
        @Sql(sql = "select * from User where name = ${T(com.jfirer.jsql.test.vo.User).xx}", paramNames = "")
        List<User> query();

        @Sql(sql = "update User set name = ${T(com.jfirer.jsql.test.vo.User).xx}", paramNames = "")
        int update();
    }

    @Mapper
    public interface test_4
    {
        @Sql(sql = "select * from User <%if(i>5) {%>where name ='kx'<%}%> ", paramNames = "i")
        List<User> query(int i);

        @Sql(sql = "update User set name = ${user.name} <%if( user.name != null && user.name=='ss' ) {%> where name ='x' <%}%> ", paramNames = "user")
        int update(User user);
    }

    /**
     * 测试enum的转化
     */
    @Test
    public void test_6()
    {
        build(test_6.class);
    }

    @Mapper
    public interface test_6
    {
        @Sql(sql = "select * from User where state = ${T(com.jfirer.jsql.test.vo.User$State).off}", paramNames = "")
        User find4();
    }

    /**
     * 测试对$~符号
     */
    @Test
    public void test_7()
    {
        build(test_7.class);
    }

    @Mapper
    public interface test_7
    {
        @Sql(sql = "delete from User where id in ~{ids}", paramNames = "ids")
        int delete(String ids);

        @Sql(sql = "delete from User where id in ~{ids}", paramNames = "ids")
        int delete(int[] ids);

        @Sql(sql = "delete from User where id in ~{ids}", paramNames = "ids")
        int delete(Integer[] ids);

        @Sql(sql = "delete from User where id in ~{ids}", paramNames = "ids")
        int delete(long[] ids);

        @Sql(sql = "delete from User where id in ~{ids}", paramNames = "ids")
        int delete(Long[] ids);
    }

    /**
     * 测试返回值是int类型
     */
    @Test
    public void test_8()
    {
        build(test_8.class);
    }

    @Mapper
    public interface test_8
    {
        @Sql(sql = "select count(*) from user", paramNames = "")
        int count();
    }

    /**
     * 测试{}符号
     */
    @Test
    public void test_9()
    {
        build(test_9.class);
    }

    @Mapper
    public interface test_9
    {
        @Sql(sql = "select count(*) from #{name}", paramNames = "name")
        int count(String name);
    }

    @Mapper
    public interface test_10
    {
        @Sql(sql = "select count(*) <%if(name == 's') {%> from #{name} ", paramNames = "name")
        int count(String name);
    }

    @Mapper
    public interface test_11
    {
        @Sql(sql = "select sum(age) from User", paramNames = "")
        int count();
    }

    /**
     * 测试内置标准函数
     */
    @Test
    public void test_11()
    {
        build(test_11.class);
    }
}
