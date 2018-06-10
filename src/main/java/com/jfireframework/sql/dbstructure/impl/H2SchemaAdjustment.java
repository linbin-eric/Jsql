package com.jfireframework.sql.dbstructure.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.ColumnDef;
import com.jfireframework.sql.dbstructure.SchemaAdjustment;
import com.jfireframework.sql.dbstructure.TableDef;
import com.jfireframework.sql.util.TableEntityInfo;
import com.jfireframework.sql.util.TableMode;

public class H2SchemaAdjustment implements SchemaAdjustment
{
    private String              dropTable = "DROP TABLE IF EXISTS ";
    private static final Logger logger    = LoggerFactory.getLogger(H2SchemaAdjustment.class);
    
    @Override
    public void adjust(TableMode mode, DataSource dataSource, Set<TableEntityInfo> tableEntityInfos) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }
    
    protected void createTable(DataSource dataSource, Set<TableEntityInfo> tableEntityInfos) throws SQLException
    {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        for (TableEntityInfo each : tableEntityInfos)
        {
            _createTable(each, connection);
        }
        connection.commit();
        connection.setAutoCommit(true);
        connection.close();
    }
    
    private void _createTable(TableEntityInfo tableEntityInfo, Connection connection) throws SQLException
    {
        Class<?> entityClass = tableEntityInfo.getEntityClass();
        Map<String, String> propertyNameToColumnNameMap = tableEntityInfo.getPropertyNameToColumnNameMap();
        TableDef tableDef = entityClass.getAnnotation(TableDef.class);
        String tableName = tableDef.tableName();
        dropTableIfExist(connection, tableName);
        createTable(connection, tableEntityInfo, propertyNameToColumnNameMap, tableDef, tableName);
    }
    
    private void dropTableIfExist(Connection connection, String tableName) throws SQLException
    {
        PreparedStatement prepareStatement = connection.prepareStatement(dropTable + tableName);
        prepareStatement.executeUpdate();
        prepareStatement.close();
    }
    
    private void createTable(Connection connection, TableEntityInfo info, Map<String, String> propertyNameToColumnNameMap, TableDef tableDef, String tableName) throws SQLException
    {
        String createTableSql = generateCreateTableSql(info, propertyNameToColumnNameMap, tableDef, tableName);
        logger.debug("traceId:{} 生成的建表语句是:{}", TRACEID.currentTraceId(), createTableSql);
        PreparedStatement prepareStatement = connection.prepareStatement(createTableSql);
        prepareStatement.executeUpdate();
        prepareStatement.close();
    }
    
    private String generateCreateTableSql(TableEntityInfo info, Map<String, String> propertyNameToColumnNameMap, TableDef tableDef, String tableName)
    {
        StringCache cache = new StringCache();
        cache.append("CREATE TABLE PUBLIC.").append(info.getTableName()).append(" (\r\n");
        for (Entry<String, Field> entry : info.getColumnNameToFieldMap().entrySet())
        {
            entry.getValue().getAnnotation(ColumnDef.class);
        }
        return null;
    }
    
}
