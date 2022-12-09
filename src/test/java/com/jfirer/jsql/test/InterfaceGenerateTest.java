package com.jfirer.jsql.test;

import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.SessionfactoryConfig;
import com.jfirer.jsql.annotation.Sql;
import com.jfirer.jsql.mapper.Mapper;
import com.jfirer.jsql.metadata.TableMode;
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
        config.setClassLoader(InterfaceGenerateTest.class.getClassLoader());
        Set<Class<?>> set = new HashSet<Class<?>>();
        set.add(User.class);
    }

    protected void build(String packageName, Class<?> ckass)
    {
        config.setScanPackage(packageName);
        SessionFactory sessionFactory = config.build();
        SqlSession     session        = sessionFactory.openSession();
        SqlSession sqlSession = sessionFactory.openSession();
        sqlSession.update("DROP TABLE IF EXISTS user", new LinkedList<>());
        sqlSession.update("DROP TABLE IF EXISTS user2", new LinkedList<>());
        sqlSession.update(userTableDml, new LinkedList<>());
        sqlSession.update(user2TableDml, new LinkedList<>());
        session.getMapper(ckass);
    }

    /**
     *
     */
    @Test
    public void test_4()
    {
        build("com.jfirer.jsql.test:in~*$test_4;com.jfirer.jsql.test.vo", test_4.class);
    }

    /**
     * 测试as
     */
    @Test
    public void test_1()
    {
        build("com.jfirer.jsql.test:in~*$test_1;com.jfirer.jsql.test.vo", test_1.class);
    }

    /**
     * 测试缺少</if>的情况
     */
    @Test
    public void test_10()
    {
        try
        {
            build("com.jfirer.jsql.test:in~*$test_10;com.jfirer.jsql.test.vo", test_10.class);
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
        build("com.jfirer.jsql.test:in~*$test_2;com.jfirer.jsql.test.vo", test_2.class);
    }

    @Mapper
    public static interface test_1
    {
        @Sql(sql = "select * from User as u where u.name= ${T(com.jfirer.jsql.test.vo.User).xx}", paramNames = "")
        public List<User> find3();
    }

    @Mapper
    public static interface test_2
    {
        @Sql(sql = "select * from User where name = ${T(com.jfirer.jsql.test.vo.User).xx}", paramNames = "")
        public List<User> query();

        @Sql(sql = "update User set name = ${T(com.jfirer.jsql.test.vo.User).xx}", paramNames = "")
        public int update();
    }

    @Mapper
    public static interface test_4
    {
        @Sql(sql = "select * from User <%if(i>5) {%>where name ='kx'<%}%> ", paramNames = "i")
        public List<User> query(int i);

        @Sql(sql = "update User set name = ${user.name} <%if( user.name != null && user.name=='ss' ) {%> where name ='x' <%}%> ", paramNames = "user")
        public int update(User user);
    }

    /**
     * 测试enum的转化
     */
    @Test
    public void test_6()
    {
        build("com.jfirer.jsql.test:in~*$test_6;com.jfirer.jsql.test.vo", test_6.class);
    }

    @Mapper
    public static interface test_6
    {
        @Sql(sql = "select * from User where state = ${T(com.jfirer.jsql.test.vo.User$State).off}", paramNames = "")
        public User find4();
    }

    /**
     * 测试对$~符号
     */
    @Test
    public void test_7()
    {
        build("com.jfirer.jsql.test:in~*$test_7;com.jfirer.jsql.test.vo", test_7.class);
    }

    @Mapper
    public static interface test_7
    {
        @Sql(sql = "delete from User where id in ~{ids}", paramNames = "ids")
        public int delete(String ids);

        @Sql(sql = "delete from User where id in ~{ids}", paramNames = "ids")
        public int delete(int[] ids);

        @Sql(sql = "delete from User where id in ~{ids}", paramNames = "ids")
        public int delete(Integer[] ids);

        @Sql(sql = "delete from User where id in ~{ids}", paramNames = "ids")
        public int delete(long[] ids);

        @Sql(sql = "delete from User where id in ~{ids}", paramNames = "ids")
        public int delete(Long[] ids);
    }

    /**
     * 测试返回值是int类型
     */
    @Test
    public void test_8()
    {
        build("com.jfirer.jsql.test:in~*$test_8;com.jfirer.jsql.test.vo", test_8.class);
    }

    @Mapper
    public static interface test_8
    {
        @Sql(sql = "select count(*) from user", paramNames = "")
        public int count();
    }

    /**
     * 测试{}符号
     */
    @Test
    public void test_9()
    {
        build("com.jfirer.jsql.test:in~*$test_9;com.jfirer.jsql.test.vo", test_9.class);
    }

    @Mapper
    public static interface test_9
    {
        @Sql(sql = "select count(*) from #{name}", paramNames = "name")
        public int count(String name);
    }

    @Mapper
    public static interface test_10
    {
        @Sql(sql = "select count(*) <%if(name == 's') {%> from #{name} ", paramNames = "name")
        public int count(String name);
    }

    @Mapper
    public static interface test_11
    {
        @Sql(sql = "select sum(age) from User", paramNames = "")
        public int count();
    }

    /**
     * 测试内置标准函数
     */
    @Test
    public void test_11()
    {
        build("com.jfirer.jsql.test:in~*$test_11;com.jfirer.jsql.test.vo", test_11.class);
    }
}
