package com.jfireframework.sql.dbstructure.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.idstrategy.AutoIncrement;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.metadata.TableMetaData;

public class MariaDBStructure extends AbstractDBStructure
{
    
    public MariaDBStructure(String schema)
    {
        super(schema);
    }
    
    @Override
    protected String buildCreateTableSql(TableMetaData tableMetaData)
    {
        String tableName = schema + "." + tableMetaData.getTableName();
        StringCache cache = new StringCache();
        cache.append("CREATE TABLE ").append(tableName).append(" (");
        for (MapField each : tableMetaData.getFieldInfos())
        {
            cache.append(each.getColName()).append(' ').append(getDesc(each, tableMetaData)).appendComma();
        }
        cache.append("CONSTRAINT ").append(tableMetaData.getIdInfo().getColName()).append("_PK").append(" PRIMARY KEY (").append(tableMetaData.getIdInfo().getColName()).append("))");
        return cache.toString();
    }
    
    @Override
    protected String getDbColumnDataType(Connection connection, String tableName, MapField each) throws SQLException
    {
        String queryColumnTemplate = "select DATA_TYPE from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='{}' and TABLE_SCHEMA='{}' and COLUMN_NAME='{}'";
        ResultSet executeQuery = connection.prepareStatement(StringUtil.format(queryColumnTemplate, tableName, schema, each.getColName())).executeQuery();
        return executeQuery.next() ? executeQuery.getString(1) : null;
    }
    
    @Override
    protected void updateColumn(Connection connection, TableMetaData tableMetaData, String tableName, MapField each) throws SQLException
    {
        String traceId = TRACEID.currentTraceId();
        String sql = StringUtil.format("ALTER TABLE {}.{} MODIFY {} {}", schema, tableName, each.getColName(), getDesc(each, tableMetaData));
        logger.debug("traceId:{} 执行的更新语句是:{}", traceId, sql);
        connection.prepareStatement(sql).executeUpdate();
    }
    
    @Override
    protected void deletePkConstraint(Connection connection, TableMetaData tableMetaData) throws SQLException
    {
        String tableName = tableMetaData.getTableName();
        String query = StringUtil.format("select COLUMN_NAME,COLUMN_TYPE from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='{}' and TABLE_SCHEMA='{}' and COLUMN_KEY='PRI';", tableName, schema);
        ResultSet executeQuery = connection.prepareStatement(query).executeQuery();
        if (executeQuery.next())
        {
            String columnName = executeQuery.getString(1);
            String columnType = executeQuery.getString(2);
            connection.prepareStatement(StringUtil.format("ALTER TABLE {}.{} MODIFY COLUMN {} {}", schema, tableName, columnName, columnType)).executeUpdate();
            String sql = StringUtil.format("ALTER TABLE {}.{} DROP PRIMARY KEY", schema, tableName);
            connection.prepareStatement(sql).executeUpdate();
        }
    }
    
    @Override
    protected void addColumn(Connection connection, TableMetaData tableMetaData, String tableName, MapField each) throws SQLException
    {
        String traceId = TRACEID.currentTraceId();
        String sql = StringUtil.format("ALTER TABLE {}.{} ADD {} {}", schema, tableName, each.getColName(), getDesc(each, tableMetaData));
        logger.debug("traceId:{} 执行添加列语句:{}", traceId, sql);
        connection.prepareStatement(sql).executeUpdate();
    }
    
    @Override
    protected void addPKConstraint(Connection connection, TableMetaData tableMetaData, String tableName) throws SQLException
    {
        String traceId = TRACEID.currentTraceId();
        String sql = StringUtil.format("ALTER TABLE {}.{} ADD CONSTRAINT {} PRIMARY KEY ({})", schema, tableName, tableMetaData.getIdInfo().getColName() + "_PK", tableMetaData.getIdInfo().getColName());
        logger.debug("traceId:{} 准备增加主键约束，sql为:{}", traceId, sql);
        connection.prepareStatement(sql).executeUpdate();
    }
    
    @Override
    protected void deleteUnExistColumns(Connection connection, TableMetaData tableMetaData, String tableName) throws SQLException
    {
        Set<String> columnNames = new HashSet<String>();
        for (MapField each : tableMetaData.getFieldInfos())
        {
            columnNames.add(each.getColName());
        }
        String sql = StringUtil.format("select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='{}' and TABLE_SCHEMA='{}'", tableName, schema);
        ResultSet executeQuery = connection.prepareStatement(sql).executeQuery();
        List<String> deletes = new ArrayList<String>();
        while (executeQuery.next())
        {
            String columnName = executeQuery.getString(1);
            if (columnNames.contains(columnName) == false)
            {
                deletes.add(columnName);
            }
        }
        for (String each : deletes)
        {
            connection.prepareStatement(StringUtil.format("ALTER TABLE {}.{} DROP COLUMN {}", schema, tableName, each)).executeUpdate();
        }
    }
    
    @Override
    protected boolean checkIfTableExists(Connection connection, TableMetaData metaData) throws SQLException
    {
        ResultSet executeQuery = connection.prepareStatement(StringUtil.format("select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME='{}' and TABLE_SCHEMA='{}'", metaData.getTableName(), schema)).executeQuery();
        return executeQuery.next();
    }
    
    @Override
    protected void deleteExistTable(Connection connection, TableMetaData metaData) throws SQLException
    {
        String tableName = schema + "." + metaData.getTableName();
        connection.prepareStatement("DROP TABLE IF EXISTS " + tableName).execute();
    }
    
    @Override
    protected void differentiatedUpdate(Connection connection, TableMetaData tableMetaData) throws SQLException
    {
        MapField idInfo = tableMetaData.getIdInfo();
        if (idInfo.getField().isAnnotationPresent(AutoIncrement.class))
        {
            String sql = StringUtil.format("ALTER TABLE {}.{} MODIFY COLUMN {} {}", schema, tableMetaData.getTableName(), idInfo.getColName(), getDesc(idInfo, tableMetaData) + " AUTO_INCREMENT");
            logger.debug("traceId:{} 准备执行的差异性更新sql:{}", TRACEID.currentTraceId(), sql);
            connection.prepareStatement(sql).executeUpdate();
        }
    }
    
}
