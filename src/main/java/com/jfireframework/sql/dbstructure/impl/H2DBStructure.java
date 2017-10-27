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
import com.jfireframework.sql.dbstructure.column.ColumnType;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.pkstrategy.AutoIncrement;

public class H2DBStructure extends AbstractDBStructure
{
	public H2DBStructure(String schema)
	{
		super(schema);
	}
	
	@Override
	protected void differentiatedUpdate(Connection connection, TableMetaData tableMetaData) throws SQLException
	{
		MapField idFiled = tableMetaData.getIdInfo();
		if (idFiled.getField().isAnnotationPresent(AutoIncrement.class))
		{
			String sql = StringUtil.format("ALTER TABLE {}.{} MODIFY COLUMN {} {}", schema, tableMetaData.getTableName(), idFiled.getColName(), getDesc(idFiled, tableMetaData) + " AUTO_INCREMENT");
			logger.debug("traceId:{} 准备执行的差异性更新sql:{}", TRACEID.currentTraceId(), sql);
			connection.prepareStatement(sql).executeUpdate();
		}
	}
	
	@Override
	protected String buildCreateTableSql(TableMetaData tableMetaData)
	{
		StringCache cache = new StringCache();
		cache.append("CREATE TABLE ").append(schema).append('.').append(tableMetaData.getTableName()).append('(');
		for (MapField each : tableMetaData.getFieldInfos())
		{
			cache.append(each.getColName()).append(' ').append(getDesc(each, tableMetaData)).appendComma();
		}
		cache.append("CONSTRAINT ").append(tableMetaData.getIdInfo().getColName()).append("_PK PRIMARY KEY (").append(tableMetaData.getIdInfo().getColName()).append("))");
		return cache.toString();
	}
	
	@Override
	protected void deleteExistTable(Connection connection, TableMetaData metaData) throws SQLException
	{
		connection.prepareStatement(StringUtil.format("DROP TABLE {}.{}", schema, metaData.getTableName())).executeUpdate();
	}
	
	@Override
	protected boolean checkIfTableExists(Connection connection, TableMetaData metaData) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String sql = StringUtil.format("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='{}' AND TABLE_NAME='{}'", schema, metaData.getTableName());
		logger.debug("traceId:{} 检查H2数据库表是否存在的sql是:{}", traceId, sql);
		ResultSet executeQuery = connection.prepareStatement(sql).executeQuery();
		executeQuery.next();
		int exist = executeQuery.getInt(1);
		logger.debug("traceId:{} 检查结果:{}", traceId, exist);
		return exist >= 1;
	}
	
	@Override
	protected void updateColumn(Connection connection, TableMetaData tableMetaData, String tableName, MapField each) throws SQLException
	{
		String sql = StringUtil.format("ALTER TABLE {}.{} MODIFY COLUMN {} {}", schema, tableMetaData.getTableName(), each.getColName(), getDesc(each, tableMetaData));
		connection.prepareStatement(sql).executeUpdate();
	}
	
	@Override
	protected void deletePkConstraint(Connection connection, TableMetaData tableMetaData) throws SQLException
	{
		String sql = StringUtil.format("ALTER TABLE {}.{} DROP PRIMARY KEY;", schema, tableMetaData.getTableName());
		connection.prepareStatement(sql).executeUpdate();
	}
	
	@Override
	protected void addColumn(Connection connection, TableMetaData tableMetaData, String tableName, MapField each) throws SQLException
	{
		String sql = StringUtil.format("ALTER TABLE {}.{} ADD COLUMN {} {}", schema, tableMetaData.getTableName(), each.getColName(), getDesc(each, tableMetaData));
		connection.prepareStatement(sql).executeUpdate();
	}
	
	@Override
	protected void addPKConstraint(Connection connection, TableMetaData tableMetaData, String tableName) throws SQLException
	{
		String sql = StringUtil.format("ALTER TABLE {}.{} ADD CONSTRAINT {}_PK PRIMARY KEY ({})", schema, tableName, tableMetaData.getIdInfo().getColName(), tableMetaData.getIdInfo().getColName());
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
		String sql = StringUtil.format("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='{}' AND TABLE_NAME='{}'", schema, tableName);
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
	protected boolean checkColumnDefinitionFit(Connection connection, MapField each, TableMetaData tableMetaData) throws SQLException
	{
		String sql = StringUtil.format("SELECT TYPE_NAME,CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='{}' AND TABLE_NAME='{}' AND COLUMN_NAME='{}'", schema, tableMetaData.getTableName(), tableMetaData.getIdInfo().getColName());
		ResultSet executeQuery = connection.prepareStatement(sql).executeQuery();
		executeQuery.next();
		String typeName = executeQuery.getString(1);
		String length = executeQuery.getString(2);
		ColumnType columnType = each.getColumnType();
		return typeName.equalsIgnoreCase(columnType.type()) && length.equalsIgnoreCase(columnType.desc());
	}
	
	@Override
	protected boolean columnExist(Connection connection, MapField each, TableMetaData tableMetaData) throws SQLException
	{
		String sql = StringUtil.format("SELECT count(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='{}' AND TABLE_NAME='{}' AND COLUMN_NAME='{}'", schema, tableMetaData.getTableName(), tableMetaData.getIdInfo().getColName());
		ResultSet executeQuery = connection.prepareStatement(sql).executeQuery();
		executeQuery.next();
		return executeQuery.getInt(1) > 0;
	}
	
}
