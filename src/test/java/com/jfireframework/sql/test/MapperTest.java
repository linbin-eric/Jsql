package com.jfireframework.sql.test;

import java.util.LinkedList;
import java.util.List;
import org.h2.Driver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer.CustomTransfer;
import com.jfireframework.sql.resultsettransfer.impl.EnumOrdinalTransfer;
import com.jfireframework.sql.session.SessionFactory;
import com.jfireframework.sql.session.SessionfactoryConfig;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.test.vo.User;
import com.jfireframework.sql.test.vo.User.State;
import com.jfireframework.sql.test.vo.User.StringEnum;
import com.zaxxer.hikari.HikariDataSource;

public class MapperTest
{
    public static interface TestOp
    {
        @Sql(sql = "select count(*) from {table} ", paramNames = "table")
        public int count(String table);
        
        @Sql(sql = "select count(*) from {table} where name2=$name", paramNames = "table,name")
        public int count(String table, String name);
        
        /** 测试$%%格式 **/
        @Sql(sql = "select count(*) from User where name like $%name%", paramNames = "name")
        public int count2(String name);
        
        @Sql(sql = "select count(*) from User where name like $%name", paramNames = "name")
        public int count3(String name);
        
        @Sql(sql = "select count(*) from User where name like $name%", paramNames = "name")
        public int count4(String name);
        
        /** 测试$%%格式 **/
        
        /** 测试$~ **/
        @Sql(sql = "select count(*) from User where id in $~ids", paramNames = "ids")
        public int count5(String ids);
        
        @Sql(sql = "select count(*) from User where id in $~ids", paramNames = "ids")
        public int count5(String[] ids);
        
        @Sql(sql = "select count(*) from User where id in $~ids", paramNames = "ids")
        public int count5(int[] ids);
        
        @Sql(sql = "select count(*) from User where id in $~ids", paramNames = "ids")
        public int count5(Integer[] ids);
        
        @Sql(sql = "select count(*) from User where id in $~ids", paramNames = "ids")
        public int count5(long[] ids);
        
        @Sql(sql = "select count(*) from User where id in $~ids", paramNames = "ids")
        public int count5(Long[] ids);
        
        @Sql(sql = "select count(*) from User where id in $~ids", paramNames = "ids")
        public int count5(List<Integer> ids);
        
        @Sql(sql = "select count(*) from User where id in $~ids", paramNames = "ids")
        public int count5_2(List<String> ids);
        
        /** 测试$~ **/
        
        /** 测试as 功能 **/
        @Sql(sql = "select * from User as u where u.name = $name", paramNames = "name")
        public User find(String name);
        
        @Sql(sql = "select age from User as u where u.name = $name", paramNames = "name")
        public User find2(String name);
        
        @Sql(sql = "select age as a from User as u where u.name = $name", paramNames = "name")
        public User find3(String name);
        
        /** 测试as类别名 **/
        /** 测试Enum */
        @Sql(sql = "select * from User where state =$s.ordinal()", paramNames = "s")
        public User find(State s);
        
        @Sql(sql = "select * from User where stringEnum = $v.name()", paramNames = "v")
        User find(StringEnum v);
        
        @CustomTransfer(EnumOrdinalTransfer.class)
        @Sql(sql = "select state from User where name=$name", paramNames = "name")
        public State findState(String name);
        
        @CustomTransfer(EnumOrdinalTransfer.class)
        @Sql(sql = "select state from User where name like $%name%", paramNames = "name")
        public List<State> findListState(String name);
        
        /** 测试Enum */
        
        /* 测试对POJO属性的提取 */
        /* 测试page */
        @Sql(sql = "select * from User <if( $name == \"lin\")> where name like $%name% </if>", paramNames = "name")
        public List<User> find(String name, Page page);
        
        @Sql(sql = "select * from User where name like $%name%", paramNames = "name")
        public List<User> find2(String name, Page page);
        
        /* 测试page */
        /* 静态常量 */
        @Sql(sql = "select * from User where name = @com.jfireframework.sql.test.vo.User.customName", paramNames = "")
        User find3();
        /* 静态常量 */
        
    }
    
    private SessionFactory       sessionFactory;
    private SessionfactoryConfig config;
    private TestOp               testOp;
    
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
        config.setClassLoader(MapperTest.class.getClassLoader());
        config.setTableMode("create");
        config.setScanPackage("com.jfireframework.sql.test:in~*$TestOp;com.jfireframework.sql.test.vo");
        sessionFactory = config.build();
        SqlSession session = sessionFactory.openSession();
        User user = new User();
        user.setAge(12);
        user.setName("lin");
        user.setLength(18);
        user.setState(State.off);
        user.setStringEnum(StringEnum.v1);
        session.save(user);
        user.setId(null);
        user.setState(null);
        user.setName("linbin");
        user.setStringEnum(StringEnum.v2);
        session.save(user);
        session.close();
        testOp = sessionFactory.getMapper(TestOp.class);
        sessionFactory.getOrCreateCurrentSession();
    }
    
    /**
     * 测试{}符号
     */
    @Test
    public void test_1()
    {
        Assert.assertEquals(2, testOp.count("user"));
        Assert.assertEquals(1, testOp.count("user", "lin"));
        sessionFactory.getOrCreateCurrentSession().close();
    }
    
    /**
     * 测试$%%的格式
     */
    @Test
    public void test_2()
    {
        Assert.assertEquals(2, testOp.count2("in"));
        Assert.assertEquals(2, testOp.count3("in"));
        Assert.assertEquals(1, testOp.count4("linb"));
        sessionFactory.getOrCreateCurrentSession().close();
    }
    
    /**
     * 测试$~的格式
     */
    @Test
    public void test_3()
    {
        Assert.assertEquals(2, testOp.count5("1,2"));
        Assert.assertEquals(2, testOp.count5(new String[] { "1", "2" }));
        Assert.assertEquals(2, testOp.count5("1,2,"));
        Assert.assertEquals(2, testOp.count5(new int[] { 1, 2 }));
        Assert.assertEquals(2, testOp.count5(new Integer[] { 1, 2 }));
        Assert.assertEquals(2, testOp.count5(new Integer[] { 1, 2 }));
        Assert.assertEquals(2, testOp.count5(new long[] { 1, 2 }));
        Assert.assertEquals(2, testOp.count5(new Long[] { 1l, 2l }));
        List<Integer> ids = new LinkedList<Integer>();
        ids.add(1);
        ids.add(2);
        Assert.assertEquals(2, testOp.count5(ids));
        List<String> ids2 = new LinkedList<String>();
        ids2.add("1");
        ids2.add("2");
        Assert.assertEquals(2, testOp.count5_2(ids2));
    }
    
    /**
     * 测试类的as别名功能
     */
    @Test
    public void test_4()
    {
        Assert.assertEquals(12, testOp.find("lin").getAge());
        Assert.assertEquals(12, testOp.find2("lin").getAge());
        /**
         * 因为有了别名，所以实际上是无法映射的
         */
        Assert.assertEquals(12, testOp.find3("lin").getAge());
        sessionFactory.getCurrentSession().close();
    }
    
    /**
     * 测试enum
     */
    @Test
    public void test_5()
    {
        Assert.assertNotNull(testOp.find(State.off));
        Assert.assertEquals(State.off, testOp.findState("lin"));
        List<State> list = testOp.findListState("lin");
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(State.off, list.get(0));
        Assert.assertEquals("lin", testOp.find(StringEnum.v1).getName());
        sessionFactory.getCurrentSession().close();
    }
    
    /**
     * 测试page功能
     */
    @Test
    public void test_7()
    {
        Page page = new Page();
        page.setOffset(0);
        page.setSize(1);
        page.setFetchSum(true);
        List<User> users = testOp.find("lin", page);
        Assert.assertEquals(2, page.getTotal());
        Assert.assertEquals(1, users.size());
        users = testOp.find2("lin", page);
        Assert.assertEquals(2, page.getTotal());
        Assert.assertEquals(1, users.size());
        sessionFactory.getCurrentSession().close();
    }
    
    /**
     * 测试静态常量
     */
    @Test
    public void test_8()
    {
        User user = new User();
        user.setName(User.customName);
        sessionFactory.getCurrentSession().save(user);
        Assert.assertEquals(user.getId(), testOp.find3().getId());
    }
}
