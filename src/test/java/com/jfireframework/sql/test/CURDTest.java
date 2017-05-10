package com.jfireframework.sql.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.h2.Driver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import com.jfireframework.sql.dao.LockMode;
import com.jfireframework.sql.session.SessionFactory;
import com.jfireframework.sql.session.SessionfactoryConfig;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.test.vo.User;
import com.jfireframework.sql.test.vo.User.State;
import com.zaxxer.hikari.HikariDataSource;

public class CURDTest
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
        config.setClassLoader(DbCreateTest.class.getClassLoader());
        config.setTableMode("create");
        config.setScanPackage(User.class.getPackage().getName());
        sessionFactory = config.build();
    }
    
    /**
     * 测试save方法
     * 
     * @throws SQLException
     */
    @Test
    public void test_save_insert() throws SQLException
    {
        SqlSession session = sessionFactory.openSession();
        User user = new User();
        user.setName("1221");
        user.setAge(12);
        session.save(user);
        session.close();
        session = sessionFactory.openSession();
        ResultSet resultSet = session.getConnection().prepareStatement("select age from user where name2 ='1221'").executeQuery();
        Assert.assertTrue(resultSet.next());
        Assert.assertEquals(12, resultSet.getInt(1));
        session.close();
    }
    
    /**
     * 测试更新
     * 
     * @throws SQLException
     */
    @Test
    public void test_save_update() throws SQLException
    {
        test_save_insert();
        User update = new User();
        update.setId(1);
        update.setName("kkkk");
        update.setAge(3);
        SqlSession session = sessionFactory.openSession();
        session.save(update);
        ResultSet resultSet = session.getConnection().prepareStatement("select age from user where name2 ='kkkk'").executeQuery();
        Assert.assertTrue(resultSet.next());
        Assert.assertEquals(3, resultSet.getInt(1));
        session.close();
    }
    
    /**
     * 测试新增方法
     * 
     * @throws SQLException
     */
    @Test
    public void test_insert() throws SQLException
    {
        SqlSession session = sessionFactory.openSession();
        ResultSet resultSet = session.getConnection().prepareStatement("select count(*) from user").executeQuery();
        Assert.assertTrue(resultSet.next());
        Assert.assertEquals(0, resultSet.getInt(1));
        User user = new User();
        user.setName("1221");
        user.setAge(12);
        session.insert(user);
        resultSet = session.getConnection().prepareStatement("select age from user where name2 ='1221'").executeQuery();
        Assert.assertTrue(resultSet.next());
        Assert.assertEquals(12, resultSet.getInt(1));
        session.close();
    }
    
    /**
     * 测试插入的时候具备id
     * 
     * @throws SQLException
     */
    @Test
    public void test_insert_with_id() throws SQLException
    {
        SqlSession session = sessionFactory.openSession();
        User user = new User();
        user.setId(12);
        user.setAge(12);
        session.insert(user);
        ResultSet resultSet = session.getConnection().prepareStatement("select count(*) from user where id =12").executeQuery();
        resultSet.next();
        Assert.assertEquals(1, resultSet.getInt(1));
        resultSet = session.getConnection().prepareStatement("select count(*) from user ").executeQuery();
        resultSet.next();
        Assert.assertEquals(1, resultSet.getInt(1));
        session.close();
    }
    
    /**
     * 测试get方法
     * 
     * @throws SQLException
     */
    @Test
    public void test_get() throws SQLException
    {
        test_save_insert();
        SqlSession session = sessionFactory.openSession();
        User query = session.get(User.class, 1);
        Assert.assertNotNull(query);
        Assert.assertEquals(1, query.getId().intValue());
        Assert.assertEquals(new Date(User.now).getTime(), query.getDate().getTime(), 1);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        calendar.setTimeInMillis(User.now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(new Time(User.now - calendar.getTimeInMillis() - 8 * 60 * 60 * 1000), query.getTime());
        Assert.assertEquals(calendar.getTimeInMillis(), query.getSqlDate().getTime());
        Assert.assertEquals(2.53d, query.getD1(), 0.001);
        Assert.assertEquals(5.36f, query.getF1(), 0.001);
        Assert.assertEquals(23l, query.getL1());
        Assert.assertEquals(new Timestamp(User.now), query.getTimestamp());
        Assert.assertEquals(Boolean.FALSE, query.getB11());
        Assert.assertEquals(6.32d, query.getD11(), 0.001);
        Assert.assertEquals(5.69f, query.getF11(), 0.001);
        Assert.assertEquals(Long.valueOf(5625l), query.getL11());
        session.close();
    }
    
    /**
     * 测试删除方法
     * 
     * @throws SQLException
     */
    @Test
    public void test_delete() throws SQLException
    {
        test_insert();
        User delete = new User();
        delete.setId(1);
        SqlSession session = sessionFactory.openSession();
        session.delete(delete);
        ResultSet resultSet = session.getConnection().prepareStatement("select count(*) from user").executeQuery();
        Assert.assertTrue(resultSet.next());
        Assert.assertEquals(0, resultSet.getInt(1));
        session.close();
    }
    
    /**
     * 测试载入忽略
     * 
     * @throws SQLException
     */
    @Test
    public void test_loadIgnore() throws SQLException
    {
        User user = new User();
        user.setAge(12);
        user.setLength(12);
        SqlSession session = sessionFactory.openSession();
        session.save(user);
        ResultSet resultSet = session.getConnection().prepareStatement("select count(*) from user where length = 12").executeQuery();
        resultSet.next();
        Assert.assertEquals(1, resultSet.getInt(1));
        User query = session.get(User.class, 1);
        Assert.assertNull(query.getLength());
        session.close();
    }
    
    /**
     * 测试保存忽略
     * 
     * @throws SQLException
     */
    @Test
    public void test_saveIgnore() throws SQLException
    {
        User user = new User();
        user.setAge(12);
        user.setAge2(13);
        SqlSession session = sessionFactory.openSession();
        session.save(user);
        ResultSet resultSet = session.getConnection().prepareStatement("select age from User where id = 1").executeQuery();
        resultSet.next();
        System.out.println(resultSet.getInt(1));
        User query = session.get(User.class, user.getId());
        Assert.assertEquals(12, query.getAge2());
        Assert.assertEquals(12, query.getAge());
        session.close();
    }
    
    /**
     * 测试共享读取<br/>
     * ps:h2 数据库似乎不支持这个语法
     * 
     * @throws SQLException
     * @throws InterruptedException
     */
    @Test
    @Ignore
    public void test_get_locksharemode() throws SQLException, InterruptedException
    {
        test_insert();
        final AtomicInteger count = new AtomicInteger(0);
        final SessionFactory one = sessionFactory;
        final CountDownLatch latch = new CountDownLatch(2);
        for (int i = 0; i < 2; i++)
        {
            new Thread(new Runnable() {
                
                @Override
                public void run()
                {
                    latch.countDown();
                    SqlSession session = one.openSession();
                    session.beginTransAction(0);
                    session.get(User.class, 1, LockMode.SHARE);
                    count.incrementAndGet();
                    session.close();
                }
            }).start();
        }
        latch.await();
        Thread.sleep(500);
        Assert.assertEquals(2, count.get());
    }
    
    /**
     * 测试共享读取
     * 
     * @throws SQLException
     * @throws InterruptedException
     */
    @Test
    public void test_get_lockforupdate() throws SQLException, InterruptedException
    {
        test_insert();
        final AtomicInteger count = new AtomicInteger(0);
        final SessionFactory one = sessionFactory;
        final CountDownLatch latch = new CountDownLatch(2);
        for (int i = 0; i < 2; i++)
        {
            new Thread(new Runnable() {
                
                @Override
                public void run()
                {
                    latch.countDown();
                    SqlSession session = one.openSession();
                    session.beginTransAction(0);
                    session.get(User.class, 1, LockMode.UPDATE);
                    count.incrementAndGet();
                    try
                    {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    session.commit();
                    session.close();
                }
            }).start();
        }
        latch.await();
        Thread.sleep(200);
        Assert.assertEquals(1, count.get());
    }
    
    /**
     * 测试批量保存
     */
    @Test
    public void test_batchInsert()
    {
        List<User> users = new LinkedList<User>();
        User one = new User();
        one.setName("11");
        one.setAge(12);
        one.setLength(12);
        one.setState(State.off);
        users.add(one);
        User two = new User();
        two.setAge(14);
        two.setName("14");
        two.setLength(12);
        two.setState(State.off);
        users.add(two);
        SqlSession session = sessionFactory.openSession();
        session.batchInsert(users);
        Assert.assertEquals(2, session.count(User.class, ""));
    }
}
