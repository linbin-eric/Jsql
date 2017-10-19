package com.jfireframework.sql.test.oracletest;

import org.junit.Test;
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.SqlSession;
import com.zaxxer.hikari.HikariDataSource;

public class OracleTest
{
	@Test
	public void test()
	{
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:oracle:thin:@172.18.169.20:1521/orcl");
		dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		dataSource.setUsername("linbin_demo");
		dataSource.setPassword("demo");
		SessionfactoryConfig config = new SessionfactoryConfig();
		config.setDataSource(dataSource);
		config.setScanPackage("com.jfireframework.sql.test.oracletest");
		config.setTableMode("create");
		config.setSchema("linbin_demo");
		SessionFactory sessionFactory = config.build();
		SqlSession session = sessionFactory.openSession();
	}
}
