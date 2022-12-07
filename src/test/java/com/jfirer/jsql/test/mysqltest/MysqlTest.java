package com.jfirer.jsql.test.mysqltest;

import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.SessionfactoryConfig;
import com.jfirer.jsql.metadata.TableMode;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.session.SqlSession;
import com.mysql.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MysqlTest
{
    @Test
    @Ignore
    public void test() throws SQLException
    {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://172.18.169.18:13306/test_demo?characterEncoding=utf8");
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        SessionfactoryConfig config = new SessionfactoryConfig();
        config.setDataSource(dataSource);
        config.setScanPackage(MysqlTable1.class.getPackage().getName() + ":out~*.MysqlTable2");
        config.setTableMode(TableMode.CREATE);
        SessionFactory sessionFactory = config.build();
        java.sql.Date  date           = new java.sql.Date(System.currentTimeMillis());
        Calendar       calendar       = Calendar.getInstance();
        Model model = Model.insert(MysqlTable1.class)//
                           .insert("col1", 1)//
                           .insert("col2", 56l)//
                           .insert("col3", 4.65f)//
                           .insert("col4", 5.65d)//
                           .insert("col5", "2")//
                           .insert("col6", true)//
                           .insert("col7", date)//
                           .insert("col8", calendar)//
                           .insert("col9", new Timestamp(System.currentTimeMillis()))//
                           .insert("col10", new Time(System.currentTimeMillis()))//
                           .insert("col11", new byte[10])//
                           .insert("col12", "112");
        SqlSession session = sessionFactory.openSession();
        session.insert(model);
        MysqlTable1 one = session.findOne(Model.query(MysqlTable1.class).where("col1", 1));
        assertEquals(Integer.valueOf(1), one.getId());
        assertEquals(1, one.getCol1());
        assertEquals(56l, one.getCol2());
        assertEquals(4.65f, one.getCol3(), 0.0001);
        assertEquals(5.65d, one.getCol4(), 0.00001);
        assertEquals("2", one.getCol5());
        assertTrue(one.isCol6());
        assertEquals(date.toString(), one.getCol7().toString());
        calendar.set(Calendar.MILLISECOND, 0);
        assertEquals(calendar, one.getCol8());
        config = new SessionfactoryConfig();
        config.setDataSource(dataSource);
        config.setScanPackage(MysqlTable1.class.getPackage().getName() + ":out~*.MysqlTable1");
        config.setTableMode(TableMode.UPDATE);
        config.build();
    }
}
