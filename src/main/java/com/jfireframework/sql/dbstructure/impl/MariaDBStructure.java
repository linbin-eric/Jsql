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
		String traceId = TRACEID.currentTraceId();
		String tableName = tableMetaData.getTableName();
		String query = StringUtil.format("select COLUMN_NAME,COLUMN_TYPE from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='{}' and TABLE_SCHEMA='{}' and COLUMN_KEY='PRI';", tableName, schema);
		logger.debug("traceId:{} 准备删除主键约束，执行的sql:{}", traceId, query);
		ResultSet executeQuery = connection.prepareStatement(query).executeQuery();
		if (executeQuery.next())
		{
			String columnName = executeQuery.getString(1);
			String columnType = executeQuery.getString(2);
			connection.prepareStatement(StringUtil.format("ALTER TABLE {}.{} MODIFY COLUMN {} {}", schema, tableName, columnName, columnType)).executeUpdate();
			String sql = StringUtil.format("ALTER TABLE {}.{} DROP PRIMARY  KEY", schema, tableName);
			connection.prepareStatement(sql).executeUpdate();
		}
		else
		{
			logger.debug("traceId:{} 表:{}不存在主键", traceId, tableMetaData.getTableName());
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
		String sql = StringUtil.format("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='{}' and TABLE_SCHEMA='{}'", metaData.getTableName(), schema);
		ResultSet executeQuery = connection.prepareStatement(sql).executeQuery();
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
	
	@Override
	protected boolean checkColumnDefinitionFit(Connection connection, MapField each, TableMetaData tableMetaData) throws SQLException
	{
		String sql = StringUtil.format("select COLUMN_TYPE from information_schema.`COLUMNS` where TABLE_SCHEMA='{}' and TABLE_NAME='{}' and COLUMN_NAME='{}'", schema, tableMetaData.getTableName(), each.getColName());
		String columnType = getColumnType(each, tableMetaData);
		ResultSet executeQuery = connection.prepareStatement(sql).executeQuery();
		executeQuery.next();
		String dbColumnType = executeQuery.getString(1);
		return dbColumnType.equalsIgnoreCase(columnType);
	}
	
	private String getColumnType(MapField fieldInfo, TableMetaData tableMetaData)
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
	
	@Override
	protected boolean columnExist(Connection connection, MapField each, TableMetaData tableMetaData) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String sql = StringUtil.format("select count(1) from information_schema.`COLUMNS` where TABLE_SCHEMA='{}' and TABLE_NAME='{}' and COLUMN_NAME='{}'", schema, tableMetaData.getTableName(), each.getColName());
		logger.trace("traceId:{} 执行的判断列是否存在sql为:{}", traceId, sql);
		ResultSet executeQuery = connection.prepareStatement(sql).executeQuery();
		executeQuery.next();
		return executeQuery.getInt(1) > 0;
	}
	
}
