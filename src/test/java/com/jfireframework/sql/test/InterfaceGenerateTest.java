package com.jfireframework.sql.test;

import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.metadata.TableMode;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.test.vo.User;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.Driver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        config.setTableMode(TableMode.CREATE);
        Set<Class<?>> set = new HashSet<Class<?>>();
        set.add(User.class);
    }

    protected void build(String packageName, Class<?> ckass)
    {
        config.setScanPackage(packageName);
        SessionFactory sessionFactory = config.build();
        SqlSession session = sessionFactory.openSession();
        session.getMapper(ckass);
    }

    public static interface test_1
    {
        @Sql(sql = "select * from User as u where u.name= ${T(com.jfireframework.sql.test.vo.User).xx}", paramNames = "")
        public List<User> find3();
    }

    /**
     * 测试as
     */
    @Test
    public void test_1()
    {
        build("com.jfireframework.sql.test:in~*$test_1;com.jfireframework.sql.test.vo", test_1.class);
    }

    public static interface test_2
    {
        @Sql(sql = "select * from User where name = ${T(com.jfireframework.sql.test.vo.User).xx}", paramNames = "")
        public List<User> query();

        @Sql(sql = "update User set name = ${T(com.jfireframework.sql.test.vo.User).xx}", paramNames = "")
        public int update();
    }

    /**
     * 测试静态属性
     */
    @Test
    public void test_2()
    {
        build("com.jfireframework.sql.test:in~*$test_2;com.jfireframework.sql.test.vo", test_2.class);
    }

    public static interface test_4
    {
        @Sql(sql = "select * from User <%if(i>5) {%>where name ='kx'<%}%> ", paramNames = "i")
        public List<User> query(int i);

        @Sql(sql = "update User set name = ${user.name} <%if( user.name != null && user.name=='ss' ) {%> where name ='x' <%}%> ", paramNames = "user")
        public int update(User user);
    }

    /**
     */
    @Test
    public void test_4()
    {
        build("com.jfireframework.sql.test:in~*$test_4;com.jfireframework.sql.test.vo", test_4.class);
    }

    public static interface test_6
    {
        @Sql(sql = "select * from User where state = ${T(com.jfireframework.sql.test.vo.User$State).off}", paramNames = "")
        public User find4();
    }

    /**
     * 测试enum的转化
     */
    @Test
    public void test_6()
    {
        build("com.jfireframework.sql.test:in~*$test_6;com.jfireframework.sql.test.vo", test_6.class);
    }

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
     * 测试对$~符号
     */
    @Test
    public void test_7()
    {
        build("com.jfireframework.sql.test:in~*$test_7;com.jfireframework.sql.test.vo", test_7.class);
    }

    public static interface test_8
    {
        @Sql(sql = "select count(*) from user", paramNames = "")
        public int count();
    }

    /**
     * 测试返回值是int类型
     */
    @Test
    public void test_8()
    {
        build("com.jfireframework.sql.test:in~*$test_8;com.jfireframework.sql.test.vo", test_8.class);
    }

    public static interface test_9
    {
        @Sql(sql = "select count(*) from #{name}", paramNames = "name")
        public int count(String name);
    }

    /**
     * 测试{}符号
     */
    @Test
    public void test_9()
    {
        build("com.jfireframework.sql.test:in~*$test_9;com.jfireframework.sql.test.vo", test_9.class);
    }

    public static interface test_10
    {
        @Sql(sql = "select count(*) <%if(name == 's') {%> from #{name} ", paramNames = "name")
        public int count(String name);
    }

    /**
     * 测试缺少</if>的情况
     */
    @Test
    public void test_10()
    {
        try
        {
            build("com.jfireframework.sql.test:in~*$test_10;com.jfireframework.sql.test.vo", test_10.class);
            Assert.fail();
        } catch (Exception ignored)
        {
        }
    }

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
        build("com.jfireframework.sql.test:in~*$test_11;com.jfireframework.sql.test.vo", test_11.class);
    }
}
