package com.jfireframework.sql.test.mysqltest;

import org.junit.Test;
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.SqlSession;
import com.mysql.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;

public class MysqlTest
{
	@Test
	public void test()
	{
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:mysql://172.18.169.18:13306/openapi?characterEncoding=utf-8");
		dataSource.setDriverClassName(Driver.class.getName());
		dataSource.setUsername("root");
		dataSource.setPassword("root");
		SessionfactoryConfig config = new SessionfactoryConfig();
		config.setDataSource(dataSource);
		config.setScanPackage("com.jfireframework.sql.test.mysqltest");
		config.setSchema("openapi");
		config.setTableMode("create");
		SessionFactory sessionFactory = config.build();
		SqlSession session = sessionFactory.openSession();
	}
}
