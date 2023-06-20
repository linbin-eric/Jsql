package com.jfirer.jsql.test;

import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.SessionfactoryConfig;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.Param;
import com.jfirer.jsql.model.support.LockMode;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.test.vo.SqlLog;
import com.jfirer.jsql.test.vo.User;
import com.jfirer.jsql.test.vo.User3;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.Driver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CURDTest
{
    private       SessionFactory sessionFactory;
    public static String         user2TableDml = """
                                                 CREATE TABLE PUBLIC.user2 (
                                                 name2 VARCHAR(64) ,
                                                 id VARCHAR(64) ,
                                                 age INTEGER
                                                 )
                                                 """;
    public static String         userTableDml  = """
                                                 CREATE TABLE PUBLIC.user (
                                                 calendar TIMESTAMP(3) ,
                                                 date TIMESTAMP(3) ,
                                                 b BOOLEAN ,
                                                 _b11 BOOLEAN ,
                                                 l1 BIGINT ,
                                                 _d11 DOUBLE ,
                                                 string_enum varchar(64) ,
                                                 _f11 DOUBLE ,
                                                 f1 DOUBLE ,
                                                 d1 DOUBLE ,
                                                 _l11 BIGINT ,
                                                 sql_date TIMESTAMP ,
                                                 n BIGINT ,
                                                 barray BLOB ,
                                                 name2 VARCHAR(64) ,
                                                 id INTEGER AUTO_INCREMENT,
                                                 state integer ,
                                                 time TIME ,
                                                 age INTEGER ,
                                                 timestamp TIMESTAMP(3)
                                                 )
                                                 """;

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
     * 测试save方法
     *
     * @throws SQLException
     */
    @Test
    public void test_save_insert() throws SQLException
    {
        SqlSession session = sessionFactory.openSession();
        User       user    = new User();
        user.setName("1221");
        user.setAge(12);
        session.save(user);
        session.close();
        session = sessionFactory.openSession();
        ResultSet resultSet = session.getConnection().prepareStatement("select age,id from user where name2 ='1221'").executeQuery();
        Assert.assertTrue(resultSet.next());
        Assert.assertEquals(12, resultSet.getInt(1));
        assertEquals(user.getId().intValue(),resultSet.getInt(2));
        session.close();
    }

    /**
     * 测试更新
     *
     * @throws SQLException
     */
    @Test
    public void test_update() throws SQLException
    {
        test_save_insert();
        User update = new User();
        update.setId(1);
        update.setName("kkkk");
        update.setAge(3);
        SqlSession session = sessionFactory.openSession();
        session.update(update);
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
        SqlSession session   = sessionFactory.openSession();
        ResultSet  resultSet = session.getConnection().prepareStatement("select count(*) from user").executeQuery();
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
        User       user    = new User();
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
        User       query   = session.findOne(Model.selectAll().from(User.class).where(Param.eq(User::getId, 1)));
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
        Assert.assertEquals(23L, query.getL1());
        Assert.assertEquals(new Timestamp(User.now).getTime(), query.getTimestamp().getTime(), 1);
        assertEquals(User.now, query.getN());
        assertEquals(User.now, new Timestamp(User.now).getTime());
        assertEquals(User.now, query.getTimestamp().getTime(), 40);
        Assert.assertEquals(Boolean.FALSE, query.getB11());
        Assert.assertEquals(6.32d, query.getD11(), 0.001);
        Assert.assertEquals(5.69f, query.getF11(), 0.001);
        Assert.assertEquals(Long.valueOf(5625L), query.getL11());
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
        SqlSession session = sessionFactory.openSession();
        session.execute(Model.deleteFrom(User.class).where(Param.eq(User::getId, 1)));
        ResultSet resultSet = session.getConnection().prepareStatement("select count(*) from user").executeQuery();
        Assert.assertTrue(resultSet.next());
        Assert.assertEquals(0, resultSet.getInt(1));
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
        User query = session.findOne(Model.selectAll().from(User.class).where(Param.eq(User::getId, user.getId())));
        Assert.assertEquals(0, query.getAge2());
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
        final AtomicInteger  count = new AtomicInteger(0);
        final SessionFactory one   = sessionFactory;
        final CountDownLatch latch = new CountDownLatch(2);
        for (int i = 0; i < 2; i++)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    latch.countDown();
                    SqlSession session = one.openSession();
                    session.beginTransAction();
                    session.findList(Model.selectAll().from(User.class).where(Param.eq(User::getId, 1)).lockMode(LockMode.SHARE));
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
        final AtomicInteger  count = new AtomicInteger(0);
        final SessionFactory one   = sessionFactory;
        final CountDownLatch latch = new CountDownLatch(2);
        for (int i = 0; i < 2; i++)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    latch.countDown();
                    SqlSession session = one.openSession();
                    session.beginTransAction();
                    session.findList(Model.selectAll().from(User.class).where(Param.eq(User::getId, 1)).lockMode(LockMode.UPDATE));
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
     * 测试主键自动生成
     */
    @Test
    public void test()
    {
        SqlSession session = sessionFactory.openSession();
        User3      user3   = new User3();
        user3.setName("121");
        session.save(user3);
        User3 one = session.findOne(Model.selectAll().from(User3.class).where(Param.eq(User3::getName, "121")));
        assertNotNull(one.getId());
        System.out.println(one.getId());
    }
}
