package com.jfireframework.sql.dbstructure.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.Index;
import com.jfireframework.sql.annotation.Pk;
import com.jfireframework.sql.annotation.pkstrategy.AutoIncrement;
import com.jfireframework.sql.dbstructure.column.ColumnType;
import com.jfireframework.sql.dbstructure.column.MapColumn;
import com.jfireframework.sql.metadata.TableMetaData;

public class H2DBStructure extends AbstractDBStructure
{
    public H2DBStructure(String schema)
    {
        super(schema);
    }
    
    @Override
    protected void differentiatedUpdate(Connection connection, TableMetaData<?> tableMetaData) throws SQLException
    {
        MapColumn idFiled = tableMetaData.getPkColumn();
        if (idFiled.getField().isAnnotationPresent(AutoIncrement.class))
        {
            String sql = StringUtil.format("ALTER TABLE {}.{} MODIFY COLUMN {} {}", schema, tableMetaData.getTableName(), idFiled.getColName(), getDesc(idFiled, tableMetaData) + " AUTO_INCREMENT");
            logger.debug("traceId:{} 准备执行的差异性更新sql:{}", TRACEID.currentTraceId(), sql);
            PreparedStatement prepareStatement = connection.prepareStatement(sql);
            prepareStatement.executeUpdate();
            prepareStatement.close();
        }
    }
    
    @Override
    protected String buildCreateTableSql(TableMetaData<?> tableMetaData)
    {
        StringCache cache = new StringCache();
        cache.append("CREATE TABLE ").append(schema).append('.').append(tableMetaData.getTableName()).append('(');
        for (MapColumn each : tableMetaData.getAllColumns().values())
        {
            cache.append(each.getColName()).append(' ').append(getDesc(each, tableMetaData)).appendComma();
        }
        Pk pk = tableMetaData.getPkColumn().getField().getAnnotation(Pk.class);
        String pkName = "".equals(pk.pkName()) ? StringUtil.format("PK_{}", tableMetaData.getTableName().hashCode() & 0x7fffffff) : pk.pkName();
        cache.append("CONSTRAINT ").append(pkName).append(" PRIMARY KEY (").append(tableMetaData.getPkColumn().getColName()).append("))");
        return cache.toString();
    }
    
    @Override
    protected void deleteExistTable(Connection connection, TableMetaData<?> metaData) throws SQLException
    {
        PreparedStatement prepareStatement = connection.prepareStatement(StringUtil.format("DROP TABLE {}.{}", schema, metaData.getTableName()));
        prepareStatement.executeUpdate();
        prepareStatement.close();
    }
    
    @Override
    protected boolean checkIfTableExists(Connection connection, TableMetaData<?> metaData) throws SQLException
    {
        String traceId = TRACEID.currentTraceId();
        String sql = StringUtil.format("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='{}' AND TABLE_NAME='{}'", schema, metaData.getTableName());
        logger.debug("traceId:{} 检查H2数据库表是否存在的sql是:{}", traceId, sql);
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        ResultSet executeQuery = prepareStatement.executeQuery();
        executeQuery.next();
        int exist = executeQuery.getInt(1);
        logger.debug("traceId:{} 检查结果:{}", traceId, exist);
        boolean result = exist >= 1;
        prepareStatement.close();
        return result;
    }
    
    @Override
    protected void updateColumn(Connection connection, TableMetaData<?> tableMetaData, String tableName, MapColumn each) throws SQLException
    {
        String sql = StringUtil.format("ALTER TABLE {}.{} MODIFY COLUMN {} {}", schema, tableMetaData.getTableName(), each.getColName(), getDesc(each, tableMetaData));
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        prepareStatement.executeUpdate();
        prepareStatement.close();
    }
    
    @Override
    protected void deletePkConstraint(Connection connection, TableMetaData<?> tableMetaData) throws SQLException
    {
        String sql = StringUtil.format("ALTER TABLE {}.{} DROP PRIMARY KEY;", schema, tableMetaData.getTableName());
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        prepareStatement.executeUpdate();
    }
    
    @Override
    protected void addColumn(Connection connection, TableMetaData<?> tableMetaData, String tableName, MapColumn each) throws SQLException
    {
        String sql = StringUtil.format("ALTER TABLE {}.{} ADD COLUMN {} {}", schema, tableMetaData.getTableName(), each.getColName(), getDesc(each, tableMetaData));
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        prepareStatement.executeUpdate();
        prepareStatement.close();
    }
    
    @Override
    protected void addPKConstraint(Connection connection, TableMetaData<?> tableMetaData, String tableName) throws SQLException
    {
        String pkName = StringUtil.format("PK_{}", tableName.hashCode() & 0x7fffffff);
        String sql = StringUtil.format("ALTER TABLE {}.{} ADD CONSTRAINT {} PRIMARY KEY ({})", schema, tableName, pkName, tableMetaData.getPkColumn().getColName());
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    @Override
    protected void deleteUnExistColumns(Connection connection, TableMetaData<?> tableMetaData, String tableName) throws SQLException
    {
        Set<String> columnNames = new HashSet<String>();
        for (MapColumn each : tableMetaData.getAllColumns().values())
        {
            columnNames.add(each.getColName());
        }
        String sql = StringUtil.format("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='{}' AND TABLE_NAME='{}'", schema, tableName);
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        ResultSet executeQuery = prepareStatement.executeQuery();
        List<String> deletes = new ArrayList<String>();
        while (executeQuery.next())
        {
            String columnName = executeQuery.getString(1);
            if (columnNames.contains(columnName) == false)
            {
                deletes.add(columnName);
            }
        }
        prepareStatement.close();
        for (String each : deletes)
        {
            prepareStatement = connection.prepareStatement(StringUtil.format("ALTER TABLE {}.{} DROP COLUMN {}", schema, tableName, each));
            prepareStatement.executeUpdate();
        }
    }
    
    @Override
    protected boolean checkColumnDefinitionFit(Connection connection, MapColumn each, TableMetaData<?> tableMetaData) throws SQLException
    {
        String sql = StringUtil.format("SELECT TYPE_NAME,CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='{}' AND TABLE_NAME='{}' AND COLUMN_NAME='{}'", schema, tableMetaData.getTableName(), tableMetaData.getPkColumn().getColName());
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        ResultSet executeQuery = prepareStatement.executeQuery();
        executeQuery.next();
        String typeName = executeQuery.getString(1);
        String length = executeQuery.getString(2);
        ColumnType columnType = each.getColumnType();
        prepareStatement.close();
        return typeName.equalsIgnoreCase(columnType.type()) && length.equalsIgnoreCase(columnType.desc());
    }
    
    @Override
    protected boolean columnExist(Connection connection, MapColumn each, TableMetaData<?> tableMetaData) throws SQLException
    {
        String sql = StringUtil.format("SELECT count(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='{}' AND TABLE_NAME='{}' AND COLUMN_NAME='{}'", schema, tableMetaData.getTableName(), tableMetaData.getPkColumn().getColName());
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        ResultSet executeQuery = prepareStatement.executeQuery();
        executeQuery.next();
        boolean result = executeQuery.getInt(1) > 0;
        prepareStatement.close();
        return result;
    }
    
    @Override
    protected void setComment(MapColumn mapField, TableMetaData<?> tableMetaData, Connection connection) throws SQLException
    {
        // 暂不支持，该数据库找到添加注释的地方
    }
    
    @Override
    protected void setIndex(MapColumn mapColumn, TableMetaData<?> tableMetaData, Connection connection) throws SQLException
    {
        Index index = mapColumn.getField().getAnnotation(Index.class);
        String indexName = "".equals(index.indexName()) ? "IDX_" + ((tableMetaData.getTableName() + ":" + mapColumn.getColName()).hashCode() & 0x7fffffff) : index.indexName();
        String createIndexSql = null;
        if (index.unique())
        {
            createIndexSql = StringUtil.format("CREATE UNIQUE INDEX {} ON {}.{} ({}) ;", indexName, schema, tableMetaData.getTableName(), mapColumn.getColName());
        }
        else
        {
            createIndexSql = StringUtil.format("CREATE INDEX {} ON {}.{} ({}) ;", indexName, schema, tableMetaData.getTableName(), mapColumn.getColName());
        }
        PreparedStatement prepareStatement = connection.prepareStatement(createIndexSql);
        prepareStatement.executeUpdate();
        prepareStatement.close();
    }
    
    @Override
    protected void deleteAllIndexs(Connection connection, TableMetaData<?> tableMetaData) throws SQLException
    {
        String selectIndexs = StringUtil.format("SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_SCHEMA='{}' AND TABLE_NAME='{}'", schema, tableMetaData.getTableName());
        PreparedStatement prepareStatement = connection.prepareStatement(selectIndexs);
        ResultSet query = prepareStatement.executeQuery();
        List<String> indexNames = new ArrayList<String>();
        while (query.next())
        {
            indexNames.add(query.getString(1));
        }
        query.close();
        prepareStatement.close();
        prepareStatement = connection.prepareStatement("DROP INDEX ?");
        for (String index : indexNames)
        {
            prepareStatement.setString(1, schema + "." + index);
            prepareStatement.addBatch();
        }
        prepareStatement.executeBatch();
        prepareStatement.close();
    }
    
}
