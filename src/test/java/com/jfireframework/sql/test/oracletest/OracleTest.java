package com.jfireframework.sql.test.oracletest;

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
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.SqlSession;
import com.jfireframework.sql.dbstructure.Index;
import com.jfireframework.sql.dbstructure.column.ColumnType;
import com.jfireframework.sql.dbstructure.column.MapColumn;
import com.jfireframework.sql.metadata.TableMetaData;
import com.zaxxer.hikari.HikariDataSource;

public class OracleTest
{
    private static final Logger logger = LoggerFactory.getLogger(OracleTest.class);
    
    @Test
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
        config.setTableMode("update");
        config.setSchema("linbin_demo");
        SessionFactory sessionFactory = config.build();
        SqlSession session = sessionFactory.openSession();
        Connection connection = session.getConnection();
        String template = "SELECT DATA_TYPE,DATA_LENGTH FROM SYS.USER_TAB_COLS WHERE TABLE_NAME='{}' AND COLUMN_NAME='{}'";
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
                ColumnType columnType = mapField.getColumnType();
                if (executeQuery.getString(1).startsWith("TIMESTAMP") || executeQuery.getString(1).startsWith("DATE"))
                {
                    continue;
                }
                if (executeQuery.getString(1).equalsIgnoreCase(columnType.type()) == false)
                {
                    logger.debug("traceId:{} 比对失败，数据库类型:{},我们类型:{}", traceId, executeQuery.getString(1), columnType.type());
                }
                if (executeQuery.getString(1).equalsIgnoreCase("blob") || executeQuery.getString(1).equalsIgnoreCase("clob"))
                {
                    continue;
                }
                if (executeQuery.getString(2).equalsIgnoreCase(columnType.desc()) == false)
                {
                    logger.debug("traceId:{} 比对失败，数据库类型:{}长度:{},我们定义类型长度:{}", traceId, executeQuery.getString(1), executeQuery.getString(2), columnType.desc());
                    Assert.fail();
                }
            }
        }
        for (TableMetaData<?> tableMetaData : metaDatas)
        {
            for (MapColumn mapColumn : tableMetaData.getAllColumns().values())
            {
                if (mapColumn.getField().isAnnotationPresent(Index.class))
                {
                    PreparedStatement prepareStatement = connection.prepareStatement(StringUtil.format("SELECT * FROM SYS.USER_IND_COLUMNS WHERE TABLE_NAME='{}' AND COLUMN_NAME='{}'", tableMetaData.getTableName(), mapColumn.getColName()));
                    ResultSet executeQuery = prepareStatement.executeQuery();
                    Assert.assertTrue(executeQuery.next());
                }
            }
        }
    }
    
    @Test
    public void testRac() throws SQLException
    {
        String traceId = TRACEID.newTraceId();
        String url2 = "(DESCRIPTION =(ADDRESS_LIST=(ADDRESS = (PROTOCOL = TCP)(HOST = 172.18.169.210)(PORT = 1521))(ADDRESS = (PROTOCOL = TCP)(HOST = 172.18.169.212)(PORT = 1521)))(load_balance=yes)(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = bspjdb)))";
        url2 = "jdbc:oracle:thin:@" + url2.toUpperCase();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url2);
        dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        dataSource.setUsername("EPAY1024");
        dataSource.setPassword("bs");
        SessionfactoryConfig config = new SessionfactoryConfig();
        config.setDataSource(dataSource);
        config.setScanPackage("com.jfireframework.sql.test.oracletest");
        config.setTableMode("create");
        config.setSchema("EPAY1024");
        SessionFactory sessionFactory = config.build();
        SqlSession session = sessionFactory.openSession();
        Connection connection = session.getConnection();
        String template = "SELECT DATA_TYPE,DATA_LENGTH FROM SYS.USER_TAB_COLS WHERE TABLE_NAME='{}' AND COLUMN_NAME='{}'";
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
                ColumnType columnType = mapField.getColumnType();
                if (executeQuery.getString(1).startsWith("TIMESTAMP") || executeQuery.getString(1).startsWith("DATE"))
                {
                    continue;
                }
                if (executeQuery.getString(1).equalsIgnoreCase(columnType.type()) == false)
                {
                    logger.debug("traceId:{} 比对失败，数据库类型:{},我们类型:{}", traceId, executeQuery.getString(1), columnType.type());
                }
                if (executeQuery.getString(1).equalsIgnoreCase("blob") || executeQuery.getString(1).equalsIgnoreCase("clob"))
                {
                    continue;
                }
                if (executeQuery.getString(2).equalsIgnoreCase(columnType.desc()) == false)
                {
                    logger.debug("traceId:{} {}比对失败，数据库类型长度:{},我们定义类型长度:{}", traceId, mapField.getColName(), executeQuery.getString(2), columnType.desc());
                    Assert.fail();
                }
            }
        }
    }
}
