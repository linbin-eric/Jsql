package com.jfireframework.sql.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.h2.Driver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.test.vo.User;
import com.zaxxer.hikari.HikariDataSource;

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
        config.setSchema("PUBLIC");
        config.setClassLoader(InterfaceGenerateTest.class.getClassLoader());
        config.setTableMode("create");
        Set<Class<?>> set = new HashSet<Class<?>>();
        set.add(User.class);
    }
    
    protected void build(String packageName)
    {
        config.setScanPackage(packageName);
        config.build();
    }
    
    public static interface test_1
    {
        @Sql(sql = "select * from User as u where u.name= User.xx", paramNames = "")
        public List<User> find3();
    }
    
    /**
     * 测试as
     */
    @Test
    public void test_1()
    {
        config.setScanPackage("com.jfireframework.sql.test:in~*$test_1;com.jfireframework.sql.test.vo");
        config.build();
    }
    
    public static interface test_2
    {
        @Sql(sql = "select * from User where name = User.xx", paramNames = "")
        public List<User> query();
        
        @Sql(sql = "update User set name = User.xx", paramNames = "")
        public int update();
    }
    
    public static interface test_3
    {
        @Sql(sql = "select * from User as u where u.name = @com.jfireframework.sql.test.vo.User.xx", paramNames = "")
        public List<User> query();
        
        @Sql(sql = "update User as u set u.name = @com.jfireframework.sql.test.vo.User.xx", paramNames = "")
        public int update();
    }
    
    public static interface test_4
    {
        @Sql(sql = "select * from User <if($i>5)>where name ='kx'</if> ", paramNames = "i")
        public List<User> query(int i);
        
        @Sql(sql = "upadte User set name='x' <if($i>5)>where name ='kx'</if> ", paramNames = "i")
        public int update(int i);
    }
    
    public static interface test_5
    {
        @Sql(sql = "update User set name = $user.name <if( $user.name != null && $user.name==\"ss\" )> where name ='x' </if> ", paramNames = "user")
        public int update(User user);
    }
    
    /**
     * 测试if条件中出现字符串，自动将==转化为equals方法调用
     */
    @Test
    public void test_5()
    {
        build("com.jfireframework.sql.test:in~*$test_5;com.jfireframework.sql.test.vo");
    }
    
    public static interface test_6
    {
        @Sql(sql = "select * from User where state = State.off", paramNames = "")
        public User find4();
    }
    
    /**
     * 测试enum的转化
     */
    @Test
    public void test_6()
    {
        build("com.jfireframework.sql.test:in~*$test_6;com.jfireframework.sql.test.vo");
    }
    
    public static interface test_7
    {
        @Sql(sql = "delete from User where id in $~ids", paramNames = "ids")
        public int delete(String ids);
        
        @Sql(sql = "delete from User where id in $~ids", paramNames = "ids")
        public int delete(int[] ids);
        
        @Sql(sql = "delete from User where id in $~ids", paramNames = "ids")
        public int delete(Integer[] ids);
        
        @Sql(sql = "delete from User where id in $~ids", paramNames = "ids")
        public int delete(long[] ids);
        
        @Sql(sql = "delete from User where id in $~ids", paramNames = "ids")
        public int delete(Long[] ids);
    }
    
    /**
     * 测试对$~符号
     */
    @Test
    public void test_7()
    {
        build("com.jfireframework.sql.test:in~*$test_7;com.jfireframework.sql.test.vo");
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
        build("com.jfireframework.sql.test:in~*$test_8;com.jfireframework.sql.test.vo");
    }
    
    public static interface test_9
    {
        @Sql(sql = "select count(*) from {name}", paramNames = "name")
        public int count(String name);
    }
    
    /**
     * 测试{}符号
     */
    @Test
    public void test_9()
    {
        build("com.jfireframework.sql.test:in~*$test_9;com.jfireframework.sql.test.vo");
    }
    
    public static interface test_10
    {
        @Sql(sql = "select count(*) <if(name == \"s\")> from {name}", paramNames = "name")
        public int count(String name);
    }
    
    /**
     * 测试缺少</if>的星狂
     */
    @Test
    public void test_10()
    {
        try
        {
            build("com.jfireframework.sql.test:in~*$test_10;com.jfireframework.sql.test.vo");
            Assert.fail();
        }
        catch (Exception e)
        {
        }
    }
}
