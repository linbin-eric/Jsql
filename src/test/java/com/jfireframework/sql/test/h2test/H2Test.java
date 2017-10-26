package com.jfireframework.sql.test.h2test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.SqlSession;
import com.jfireframework.sql.dbstructure.column.ColumnType;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.util.TableNameCaseStrategy;
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
        config.setSchema("PUBLIC");
        config.setTableMode("create");
        config.setTableNameCaseStrategy(TableNameCaseStrategy.UPPER);
        SessionFactory sessionFactory = config.build();
        SqlSession session = sessionFactory.openSession();
        Connection connection = session.getConnection();
        String template = "SELECT TYPE_NAME,CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_NAME='{}' AND COLUMN_NAME='{}'";
        TableMetaData[] metaDatas = config.getMetaContext().metaDatas();
        for (TableMetaData tableMetaData : metaDatas)
        {
            if (tableMetaData.getIdInfo() == null || tableMetaData.editable() == false)
            {
                continue;
            }
            for (MapField mapField : tableMetaData.getFieldInfos())
            {
                ColumnType columnType = mapField.getColumnType();
                logger.debug("traceId:{} 查询的语句是:{}", traceId, StringUtil.format(template, tableMetaData.getTableName(), mapField.getColName()));
                ResultSet executeQuery = connection.prepareStatement(StringUtil.format(template, tableMetaData.getTableName(), mapField.getColName())).executeQuery();
                Assert.assertTrue(executeQuery.next());
                String typeName = executeQuery.getString(1);
                String length = executeQuery.getString(2);
                Assert.assertEquals(typeName, columnType.type());
                Assert.assertEquals(length, columnType.desc());
            }
        }
    }
}
