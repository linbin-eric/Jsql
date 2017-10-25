package com.jfireframework.sql.test.oracletest;

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
        config.setTableMode("create");
        config.setSchema("linbin_demo");
        SessionFactory sessionFactory = config.build();
        SqlSession session = sessionFactory.openSession();
        Connection connection = session.getConnection();
        String template = "SELECT DATA_TYPE,DATA_LENGTH FROM SYS.USER_TAB_COLS WHERE TABLE_NAME='{}' AND COLUMN_NAME='{}'";
        TableMetaData[] metaDatas = config.getMetaContext().metaDatas();
        for (TableMetaData tableMetaData : metaDatas)
        {
            if (tableMetaData.getIdInfo() == null || tableMetaData.editable() == false)
            {
                continue;
            }
            for (MapField mapField : tableMetaData.getFieldInfos())
            {
                logger.trace("traceId:{} 查询的语句是:{}", traceId, StringUtil.format(template, tableMetaData.getTableName(), mapField.getColName()));
                ResultSet executeQuery = connection.prepareStatement(StringUtil.format(template, tableMetaData.getTableName(), mapField.getColName())).executeQuery();
                Assert.assertTrue(executeQuery.next());
                ColumnType columnType = mapField.getColumnType();
                if (executeQuery.getString(1).startsWith("TIMESTAMP"))
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
                    logger.debug("traceId:{} 比对失败，数据库类型长度:{},我们定义类型长度:{}", traceId, executeQuery.getString(2), columnType.desc());
                    Assert.fail();
                }
            }
        }
    }
}
