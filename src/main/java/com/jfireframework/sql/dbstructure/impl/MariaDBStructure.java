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

public class MariaDBStructure extends AbstractDBStructure
{
	
	public MariaDBStructure(String schema)
	{
		super(schema);
	}
	
	@Override
	protected void _createTable(Connection connection, TableMetaData tableMetaData) throws SQLException
	{
		String tableName = tableMetaData.getTableName();
		MapField idInfo = tableMetaData.getIdInfo();
		StringCache cache = new StringCache();
		cache.append("CREATE TABLE ").append(tableName).append(" (");
		cache.append(idInfo.getColName()).append(' ').append(getDesc(idInfo, tableMetaData));
		if (idInfo.getField().getType() == Integer.class || idInfo.getField().getType() == Long.class)
		{
			cache.append(" AUTO_INCREMENT ");
		}
		cache.append(" primary key").appendComma();
		for (MapField each : tableMetaData.getFieldInfos())
		{
			if (each.getFieldName().equals(idInfo.getFieldName()))
			{
				continue;
			}
			try
			{
				cache.append(each.getColName()).append(' ').append(getDesc(each, tableMetaData)).appendComma();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		cache.deleteLast().append(")");
		logger.warn("进行表:{}的创建，创建语句是\n{}", tableName, cache.toString());
		connection.prepareStatement("DROP TABLE IF EXISTS " + tableName).execute();
		connection.prepareStatement(cache.toString()).execute();
	}
	
	protected void _updateTable(Connection connection, TableMetaData tableMetaData) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String tableName = tableMetaData.getTableName();
		deletePkConstraint(connection, tableMetaData);
		String queryColumnTemplate = "select DATA_TYPE from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='{}' and TABLE_SCHEMA='{}' and COLUMN_NAME='{}'";
		for (MapField each : tableMetaData.getFieldInfos())
		{
			ResultSet executeQuery = connection.prepareStatement(StringUtil.format(queryColumnTemplate, tableName, schema, each.getColName())).executeQuery();
			if (executeQuery.next())
			{
				ColumnType columnType = tableMetaData.columnType(each);
				String dataType = executeQuery.getString(1);
				if (columnType.type().equals(dataType))
				{
					logger.trace("traceId:{} 表:{}中的列:{}类型与类定义符合，不需要更新", traceId, tableName, each.getColName());
				}
				else
				{
					logger.debug("traceId:{} 表:{}中的列:{}类型:{}与类:{}的字段类型定义:{}不符合，需要更新", traceId, tableName, each.getColName(), dataType, each.getField().getDeclaringClass().getName(), columnType.type());
					updateColumn(connection, tableMetaData, tableName, each);
				}
			}
			else
			{
				logger.debug("traceId:{} 表:{}中的列:{}不存在", traceId, tableName, each.getColName());
				addColumn(connection, tableMetaData, tableName, each);
			}
		}
		addPKConstraint(connection, tableMetaData, tableName);
		deleteUnExistColumns(connection, tableMetaData, tableName);
	}
	
	private void updateColumn(Connection connection, TableMetaData tableMetaData, String tableName, MapField each) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String sql = StringUtil.format("ALTER TABLE {}.{} MODIFY {} {}", schema, tableName, each.getColName(), getDesc(each, tableMetaData));
		logger.debug("traceId:{} 执行执行的更新语句是:{}", traceId, sql);
		connection.prepareStatement(sql).executeUpdate();
	}
	
	private void deletePkConstraint(Connection connection, TableMetaData tableMetaData) throws SQLException
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
	
	private void addColumn(Connection connection, TableMetaData tableMetaData, String tableName, MapField each) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String sql = StringUtil.format("ALTER TABLE {}.{} ADD {} {}", schema, tableName, each.getColName(), getDesc(each, tableMetaData));
		logger.debug("traceId:{} 执行添加列语句:{}", traceId, sql);
		connection.prepareStatement(sql).executeUpdate();
	}
	
	private void addPKConstraint(Connection connection, TableMetaData tableMetaData, String tableName) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String sql = StringUtil.format("ALTER TABLE {}.{} ADD CONSTRAINT {} PRIMARY KEY ({})", schema, tableName, tableMetaData.getIdInfo().getColName() + "_PK", tableMetaData.getIdInfo().getColName());
		logger.debug("traceId:{} 准备增加主键约束，sql为:{}", traceId, sql);
		connection.prepareStatement(sql).executeUpdate();
	}
	
	private void deleteUnExistColumns(Connection connection, TableMetaData tableMetaData, String tableName) throws SQLException
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
			connection.prepareStatement(StringUtil.format("ALTER TABLE {} DROP COLUMN {}", tableName, each)).executeUpdate();
		}
	}
	
	@Override
	protected boolean checkIfTableExists(Connection connection, TableMetaData metaData) throws SQLException
	{
		ResultSet executeQuery = connection.prepareStatement(StringUtil.format("select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME='{}' and TABLE_SCHEMA='{}'", metaData.getTableName(), schema)).executeQuery();
		return executeQuery.next();
	}
	
}
