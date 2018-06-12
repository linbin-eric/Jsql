package com.jfireframework.sql.test.oracletest;

import java.sql.SQLException;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.metadata.TableMode;
import com.jfireframework.sql.session.SqlSession;
import com.zaxxer.hikari.HikariDataSource;

public class OracleTest
{
	private static final Logger logger = LoggerFactory.getLogger(OracleTest.class);
	
	@Test
	@Ignore
	public void test() throws SQLException
	{
		String traceId = TRACEID.newTraceId();
		String url = "jdbc:oracle:thin:@172.18.169.20:1521/orcl";
		String url2 = "jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =(ADDRESS = (PROTOCOL = TCP)(HOST = 172.18.169.20)(PORT = 1521)))(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = orcl)))";
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(url2);
		dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		dataSource.setUsername("linbin_demo");
		dataSource.setPassword("demo");
		SessionfactoryConfig config = new SessionfactoryConfig();
		config.setDataSource(dataSource);
		config.setScanPackage("com.jfireframework.sql.test.oracletest");
		config.setTableMode(TableMode.CREATE);
		SessionFactory sessionFactory = config.build();
		SqlSession session = sessionFactory.openSession();
	}
	
}
