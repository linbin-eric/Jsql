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
import com.jfireframework.sql.Comment;
import com.jfireframework.sql.annotation.pkstrategy.AutoIncrement;
import com.jfireframework.sql.dbstructure.column.ColumnType;
import com.jfireframework.sql.dbstructure.column.MapColumn;
import com.jfireframework.sql.metadata.TableMetaData;

public class MysqlDBStructure extends AbstractDBStructure
{
	
	public MysqlDBStructure(String schema)
	{
		super(schema);
	}
	
	@Override
	protected String buildCreateTableSql(TableMetaData<?> tableMetaData)
	{
		String tableName = schema + "." + tableMetaData.getTableName();
		StringCache cache = new StringCache();
		cache.append("CREATE TABLE ").append(tableName).append(" (");
		for (MapColumn each : tableMetaData.getAllColumns().values())
		{
			cache.append(each.getColName()).append(' ').append(getDesc(each, tableMetaData)).appendComma();
		}
		String pkName = StringUtil.format("PK_{}", tableName.hashCode() & 0x7fffffff);
		cache.append("CONSTRAINT ").append(pkName).append(" PRIMARY KEY (").append(tableMetaData.getPkColumn().getColName()).append("))");
		return cache.toString();
	}
	
	@Override
	protected void updateColumn(Connection connection, TableMetaData<?> tableMetaData, String tableName, MapColumn each) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String sql = StringUtil.format("ALTER TABLE {}.{} MODIFY {} {}", schema, tableName, each.getColName(), getDesc(each, tableMetaData));
		logger.debug("traceId:{} 执行的更新语句是:{}", traceId, sql);
		PreparedStatement prepareStatement = connection.prepareStatement(sql);
		prepareStatement.executeUpdate();
	}
	
	@Override
	protected void deletePkConstraint(Connection connection, TableMetaData<?> tableMetaData) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String tableName = tableMetaData.getTableName();
		String query = StringUtil.format("select COLUMN_NAME,COLUMN_TYPE from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='{}' and TABLE_SCHEMA='{}' and COLUMN_KEY='PRI';", tableName, schema);
		logger.debug("traceId:{} 准备删除主键约束，执行的sql:{}", traceId, query);
		PreparedStatement prepareStatement = connection.prepareStatement(query);
		ResultSet executeQuery = prepareStatement.executeQuery();
		if (executeQuery.next())
		{
			String columnName = executeQuery.getString(1);
			String columnType = executeQuery.getString(2);
			prepareStatement.close();
			prepareStatement = connection.prepareStatement(StringUtil.format("ALTER TABLE {}.{} MODIFY COLUMN {} {}", schema, tableName, columnName, columnType));
			prepareStatement.executeUpdate();
			prepareStatement.close();
			String sql = StringUtil.format("ALTER TABLE {}.{} DROP PRIMARY  KEY", schema, tableName);
			prepareStatement = connection.prepareStatement(sql);
			prepareStatement.executeUpdate();
			prepareStatement.close();
		}
		else
		{
			prepareStatement.close();
			logger.debug("traceId:{} 表:{}不存在主键", traceId, tableMetaData.getTableName());
		}
	}
	
	@Override
	protected void addColumn(Connection connection, TableMetaData<?> tableMetaData, String tableName, MapColumn each) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String sql = StringUtil.format("ALTER TABLE {}.{} ADD {} {}", schema, tableName, each.getColName(), getDesc(each, tableMetaData));
		logger.debug("traceId:{} 执行添加列语句:{}", traceId, sql);
		PreparedStatement prepareStatement = connection.prepareStatement(sql);
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}
	
	@Override
	protected void addPKConstraint(Connection connection, TableMetaData<?> tableMetaData, String tableName) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String pkName = StringUtil.format("PK_{}", tableName.hashCode() & 0x7fffffff);
		String sql = StringUtil.format("ALTER TABLE {}.{} ADD CONSTRAINT {} PRIMARY KEY ({})", schema, tableName, pkName, tableMetaData.getPkColumn().getColName());
		logger.debug("traceId:{} 准备增加主键约束，sql为:{}", traceId, sql);
		PreparedStatement prepareStatement = connection.prepareStatement(sql);
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}
	
	@Override
	protected void deleteUnExistColumns(Connection connection, TableMetaData<?> tableMetaData, String tableName) throws SQLException
	{
		Set<String> columnNames = new HashSet<String>();
		for (MapColumn each : tableMetaData.getAllColumns().values())
		{
			columnNames.add(each.getColName());
		}
		String sql = StringUtil.format("select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='{}' and TABLE_SCHEMA='{}'", tableName, schema);
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
			prepareStatement.close();
		}
	}
	
	@Override
	protected boolean checkIfTableExists(Connection connection, TableMetaData<?> metaData) throws SQLException
	{
		String sql = StringUtil.format("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='{}' and TABLE_SCHEMA='{}'", metaData.getTableName(), schema);
		PreparedStatement prepareStatement = connection.prepareStatement(sql);
		ResultSet executeQuery = prepareStatement.executeQuery();
		boolean result = executeQuery.next();
		prepareStatement.close();
		return result;
	}
	
	@Override
	protected void deleteExistTable(Connection connection, TableMetaData<?> metaData) throws SQLException
	{
		String tableName = schema + "." + metaData.getTableName();
		PreparedStatement preparedStatement = connection.prepareStatement("DROP TABLE IF EXISTS " + tableName);
		preparedStatement.execute();
		preparedStatement.close();
	}
	
	@Override
	protected void differentiatedUpdate(Connection connection, TableMetaData<?> tableMetaData) throws SQLException
	{
		MapColumn idInfo = tableMetaData.getPkColumn();
		if (idInfo.getField().isAnnotationPresent(AutoIncrement.class))
		{
			String sql = null;
			if (annotationUtil.isPresent(Comment.class, idInfo.getField()))
			{
				sql = StringUtil.format("ALTER TABLE {}.{} MODIFY COLUMN {} {} COMMENT '{}'", schema, tableMetaData.getTableName(), idInfo.getColName(), getDesc(idInfo, tableMetaData) + " AUTO_INCREMENT", annotationUtil.getAnnotation(Comment.class, idInfo.getField()).value());
			}
			else
			{
				sql = StringUtil.format("ALTER TABLE {}.{} MODIFY COLUMN {} {}", schema, tableMetaData.getTableName(), idInfo.getColName(), getDesc(idInfo, tableMetaData) + " AUTO_INCREMENT");
			}
			logger.debug("traceId:{} 准备执行的差异性更新sql:{}", TRACEID.currentTraceId(), sql);
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			prepareStatement.executeUpdate();
		}
	}
	
	@Override
	protected boolean checkColumnDefinitionFit(Connection connection, MapColumn each, TableMetaData<?> tableMetaData) throws SQLException
	{
		String sql = StringUtil.format("select COLUMN_TYPE from information_schema.`COLUMNS` where TABLE_SCHEMA='{}' and TABLE_NAME='{}' and COLUMN_NAME='{}'", schema, tableMetaData.getTableName(), each.getColName());
		String columnType = getColumnType(each, tableMetaData);
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		ResultSet executeQuery = preparedStatement.executeQuery();
		executeQuery.next();
		String dbColumnType = executeQuery.getString(1);
		preparedStatement.close();
		return dbColumnType.equalsIgnoreCase(columnType);
	}
	
	private String getColumnType(MapColumn fieldInfo, TableMetaData<?> tableMetaData)
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
	protected boolean columnExist(Connection connection, MapColumn each, TableMetaData<?> tableMetaData) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String sql = StringUtil.format("select count(1) from information_schema.`COLUMNS` where TABLE_SCHEMA='{}' and TABLE_NAME='{}' and COLUMN_NAME='{}'", schema, tableMetaData.getTableName(), each.getColName());
		logger.trace("traceId:{} 执行的判断列是否存在sql为:{}", traceId, sql);
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		ResultSet executeQuery = preparedStatement.executeQuery();
		executeQuery.next();
		boolean result = executeQuery.getInt(1) > 0;
		preparedStatement.close();
		return result;
	}
	
	@Override
	protected void setComment(MapColumn mapField, TableMetaData<?> tableMetaData, Connection connection) throws SQLException
	{
		Comment comment = annotationUtil.getAnnotation(Comment.class, mapField.getField());
		if (comment == null)
		{
			return;
		}
		String sql = StringUtil.format("ALTER TABLE {}.{} MODIFY COLUMN {} {}  COMMENT '{}'", schema, tableMetaData.getTableName(), mapField.getColName(), getDesc(mapField, tableMetaData), comment.value());
		PreparedStatement prepareStatement = connection.prepareStatement(sql);
		logger.debug("traceId:{} 执行的设置注释的语句是:{}", TRACEID.currentTraceId(), sql);
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}
	
}
