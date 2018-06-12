package com.jfireframework.sql.test.mysqltest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import org.junit.Ignore;
import org.junit.Test;
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.metadata.TableMode;
import com.jfireframework.sql.model.Model;
import com.jfireframework.sql.session.SqlSession;
import com.mysql.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;

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
		config.setScanPackage("com.jfireframework.sql.test.mysqltest");
		config.setTableMode(TableMode.CREATE);
		SessionFactory sessionFactory = config.build();
		java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
		Calendar calendar = Calendar.getInstance();
		Model model = Model.insert(MysqlTable.class)//
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
		SqlSession session = sessionFactory.getOrCreateCurrentSession();
		session.insert(model);
		MysqlTable one = session.findOne(Model.query(MysqlTable.class).where("col1", 1));
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
	}
	
}
