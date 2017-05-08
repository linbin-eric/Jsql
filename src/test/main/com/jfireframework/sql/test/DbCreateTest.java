package com.jfireframework.sql.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.h2.Driver;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.sql.session.SessionFactory;
import com.jfireframework.sql.session.SessionfactoryConfig;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.test.vo.User;
import com.zaxxer.hikari.HikariDataSource;

public class DbCreateTest
{
    /**
     * 创建表测试
     * 
     * @throws SQLException
     */
    @Test
    public void test() throws SQLException
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
        SessionFactory sessionFactory = config.build();
        SqlSession session = sessionFactory.openSession();
        User user = new User();
        user.setName("1221");
        session.save(user);
        User query = session.get(User.class, 1);
        Assert.assertNotNull(query);
        Assert.assertEquals("1221", query.getName());
        session.close();
        session = sessionFactory.openSession();
        Connection connection = session.getConnection();
        ResultSet resultSet = connection.prepareStatement("select count(*) from user where name2 ='1221'").executeQuery();
        Assert.assertTrue(resultSet.next());
        Assert.assertEquals(1, resultSet.getInt(1));
    }
}
