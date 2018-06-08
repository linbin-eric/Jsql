package com.jfireframework.sql.test.mysqltest;

import java.sql.SQLException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.util.TableMode;
import com.mysql.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;

public class MysqlTest
{
	private static final Logger logger = LoggerFactory.getLogger(MysqlTest.class);
	
	@Test
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
	}
	
}
