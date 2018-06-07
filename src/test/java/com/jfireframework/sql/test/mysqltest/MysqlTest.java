package com.jfireframework.sql.test.mysqltest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.SqlSession;
import com.jfireframework.sql.constant.TableNameCaseStrategy;
import com.jfireframework.sql.dbstructure.Index;
import com.jfireframework.sql.dbstructure.column.ColumnType;
import com.jfireframework.sql.dbstructure.column.MapColumn;
import com.jfireframework.sql.metadata.TableMetaData;
import com.mysql.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;

public class MysqlTest
{
    private static final Logger logger = LoggerFactory.getLogger(MysqlTest.class);
    
    @Test
    public void test() throws SQLException
    {
        String traceId = TRACEID.newTraceId();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://172.18.169.18:13306?characterEncoding=utf8");
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        SessionfactoryConfig config = new SessionfactoryConfig();
        config.setDataSource(dataSource);
        config.setScanPackage("com.jfireframework.sql.test.mysqltest");
        config.setSchema("test");
        config.setTableMode("update");
        config.setTableNameCaseStrategy(TableNameCaseStrategy.LOWER);
        SessionFactory sessionFactory = config.build();
        SqlSession session = sessionFactory.openSession();
        Connection connection = session.getConnection();
        String template = "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='test' AND TABLE_NAME='{}' AND COLUMN_NAME='{}'";
        TableMetaData<?>[] metaDatas = config.getMetaContext().metaDatas();
        for (TableMetaData<?> tableMetaData : metaDatas)
        {
            if (tableMetaData.getPkColumn() == null || tableMetaData.editable() == false)
            {
                continue;
            }
            for (MapColumn mapField : tableMetaData.getAllColumns().values())
            {
                logger.trace("traceId:{} 查询的语句是:{}", traceId, StringUtil.format(template, tableMetaData.getTableName(), mapField.getColName()));
                ResultSet executeQuery = connection.prepareStatement(StringUtil.format(template, tableMetaData.getTableName(), mapField.getColName())).executeQuery();
                Assert.assertTrue(executeQuery.next());
                Assert.assertTrue(executeQuery.getString(1).equalsIgnoreCase(getColumnType(mapField, tableMetaData)));
            }
        }
        for (TableMetaData<?> tableMetaData : metaDatas)
        {
            for (MapColumn mapColumn : tableMetaData.getAllColumns().values())
            {
                if (mapColumn.getField().isAnnotationPresent(Index.class))
                {
                    PreparedStatement prepareStatement = connection.prepareStatement(StringUtil.format("show INDEX FROM test.{} WHERE column_name = '{}'", tableMetaData.getTableName(), mapColumn.getColName()));
                    ResultSet executeQuery = prepareStatement.executeQuery();
                    Assert.assertTrue(executeQuery.next());
                }
            }
        }
    }
    
    private String getColumnType(MapColumn fieldInfo, TableMetaData tableMetaData)
    {
        StringCache cache = new StringCache();
        ColumnType columnType = fieldInfo.getColumnType();
        if (StringUtil.isNotBlank(columnType.desc()))
        {
            cache.append(columnType.type()).append('(').append(columnType.desc()).append(')');
        }
        else
        {
            cache.append(columnType.type());
        }
        return cache.toString();
    }
    
    @Test
    public void test2() throws SQLException
    {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://172.18.169.18:13306");
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        Connection connection = dataSource.getConnection();
        connection.prepareStatement("insert into test.test_demo (ID) values(NULL)").executeUpdate();
    }
}
