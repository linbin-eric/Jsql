package com.jfireframework.sql.test.h2test;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.SqlSession;
import com.jfireframework.sql.util.TableMode;
import com.zaxxer.hikari.HikariDataSource;

public class H2Test
{
    private static final Logger logger = LoggerFactory.getLogger(H2Test.class);
    
    /**
     * 表创建测试
     * 
     * @throws SQLException
     */
    @Test
    public void test() throws SQLException
    {
        String traceId = TRACEID.newTraceId();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("root");
        dataSource.setPassword("");
        createTable(traceId, dataSource);
        createTable(traceId, dataSource);
        
    }
    
    private void createTable(String traceId, HikariDataSource dataSource) throws SQLException
    {
        SessionfactoryConfig config = new SessionfactoryConfig();
        config.setDataSource(dataSource);
        config.setScanPackage("com.jfireframework.sql.test.h2test");
        config.setTableMode(TableMode.CREATE);
        SessionFactory sessionFactory = config.build();
        SqlSession session = sessionFactory.openSession();
        Connection connection = session.getConnection();
    }
}
