package com.jfireframework.sql.dbstructure.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.dbstructure.column.ColumnType;
import com.jfireframework.sql.dbstructure.column.Comment;
import com.jfireframework.sql.dbstructure.column.MapColumn;
import com.jfireframework.sql.metadata.TableMetaData;

public class OracleStructure extends AbstractDBStructure
{
	public OracleStructure(String schema)
	{
		super(schema.toUpperCase());
	}
	
	private static final Logger logger = LoggerFactory.getLogger(OracleStructure.class);
	
	@Override
	protected boolean checkIfTableExists(Connection connection, TableMetaData<?> metaData) throws SQLException
	{
		String tableName = metaData.getTableName();
		PreparedStatement prepareStatement = connection.prepareStatement(StringUtil.format("SELECT * FROM SYS.USER_TABLES t WHERE t.TABLE_NAME='{}'", tableName));
		ResultSet resultSet = prepareStatement.executeQuery();
		boolean result = resultSet.next();
		resultSet.close();
		prepareStatement.close();
		return result;
	}
	
	@Override
	protected void deleteUnExistColumns(Connection connection, TableMetaData<?> tableMetaData, String tableName) throws SQLException
	{
		Set<String> columnNames = new HashSet<String>();
		for (MapColumn each : tableMetaData.getAllColumns().values())
		{
			columnNames.add(each.getColName());
		}
		String sql = StringUtil.format("SELECT t.COLUMN_NAME FROM user_tab_columns t WHERE t.TABLE_NAME='{}'", tableName);
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		ResultSet executeQuery = preparedStatement.executeQuery();
		List<String> deletes = new ArrayList<String>();
		while (executeQuery.next())
		{
			String columnName = executeQuery.getString(1);
			if (columnNames.contains(columnName) == false)
			{
				deletes.add(columnName);
			}
		}
		executeQuery.close();
		preparedStatement.close();
		for (String each : deletes)
		{
			preparedStatement = connection.prepareStatement(StringUtil.format("ALTER TABLE {} DROP COLUMN {}", tableName, each));
			preparedStatement.executeUpdate();
			preparedStatement.close();
		}
	}
	
	@Override
	protected void addPKConstraint(Connection connection, TableMetaData<?> tableMetaData, String tableName) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String pkName = StringUtil.format("PK_{}", tableName.hashCode() & 0x7fffffff);
		String sql = StringUtil.format("ALTER TABLE {} ADD CONSTRAINT {} PRIMARY KEY ({})", tableName, pkName, tableMetaData.getPkColumn().getColName());
		logger.debug("traceId:{} 准备增加主键约束，sql为:{}", traceId, sql);
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.executeUpdate();
		preparedStatement.close();
	}
	
	@Override
	protected void addColumn(Connection connection, TableMetaData<?> tableMetaData, String tableName, MapColumn mapField) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String sql = StringUtil.format("ALTER TABLE {} ADD {} {}", tableName, mapField.getColName(), getDesc(mapField, tableMetaData));
		logger.debug("traceId:{} 执行添加列语句:{}", traceId, sql);
		PreparedStatement prepareStatement = connection.prepareStatement(sql);
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}
	
	@Override
	protected void updateColumn(Connection connection, TableMetaData<?> tableMetaData, String tableName, MapColumn mapField) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String columnType = mapField.getColumnType().type();
		if ("BLOB".equalsIgnoreCase(columnType) || "clob".equalsIgnoreCase(columnType))
		{
			PreparedStatement prepareStatement = connection.prepareStatement(StringUtil.format("ALTER TABLE {} DROP COLUMN {}", tableName, mapField.getColName()));
			prepareStatement.executeUpdate();
			prepareStatement.close();
			String sql = StringUtil.format("ALTER TABLE {} ADD {} {}", tableName, mapField.getColName(), getDesc(mapField, tableMetaData));
			logger.debug("traceId:{} 执行添加列语句:{}", traceId, sql);
			prepareStatement = connection.prepareStatement(sql);
			prepareStatement.executeUpdate();
			prepareStatement.close();
		}
		else
		{
			String sql = StringUtil.format("ALTER TABLE {} MODIFY {} {}", tableName, mapField.getColName(), getDesc(mapField, tableMetaData));
			logger.debug("traceId:{} 执行执行的更新语句是:{}", traceId, sql);
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			prepareStatement.executeUpdate();
			prepareStatement.close();
		}
	}
	
	@Override
	protected void deletePkConstraint(Connection connection, TableMetaData<?> tableMetaData) throws SQLException
	{
		String tableName = tableMetaData.getTableName();
		String traceId = TRACEID.currentTraceId();
		String findPkSql = StringUtil.format("SELECT CONSTRAINT_NAME FROM SYS.USER_CONSTRAINTS WHERE TABLE_NAME='{}' AND CONSTRAINT_TYPE='P'", tableMetaData.getTableName());
		PreparedStatement preparedStatement = connection.prepareStatement(findPkSql);
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next())
		{
			String pkName = resultSet.getString(1);
			resultSet.close();
			preparedStatement.close();
			logger.trace("traceId:{} 查询到表:{}的主键约束名称:{}", traceId, tableName, pkName);
			String deletePkSql = "ALTER TABLE " + tableName + " DROP CONSTRAINT " + pkName;
			logger.trace("traceId:{} 准备删除主键约束，执行:{}", traceId, deletePkSql);
			preparedStatement = connection.prepareStatement(deletePkSql);
			preparedStatement.executeUpdate();
			logger.trace("traceId:{} 主键约束删除完毕", traceId);
			preparedStatement.close();
		}
		else
		{
			resultSet.close();
			preparedStatement.close();
		}
	}
	
	@Override
	protected String buildCreateTableSql(TableMetaData<?> tableMetaData)
	{
		String tableName = schema + "." + tableMetaData.getTableName();
		StringCache cache = new StringCache();
		cache.append("CREATE TABLE ").append(tableName).append(" ( ");
		for (MapColumn each : tableMetaData.getAllColumns().values())
		{
			cache.append(" ").append(each.getColName()).append(' ').append(getDesc(each, tableMetaData)).appendComma();
		}
		String pkName = StringUtil.format("PK_{}", tableName.hashCode() & 0x7fffffff);
		cache.append(" CONSTRAINT ").append(pkName).append(" PRIMARY KEY (").append(tableMetaData.getPkColumn().getColName()).append("))");
		return cache.toString();
	}
	
	@Override
	protected void deleteExistTable(Connection connection, TableMetaData<?> metaData) throws SQLException
	{
		String tableName = schema + "." + metaData.getTableName();
		PreparedStatement preparedStatement = connection.prepareStatement("DROP TABLE " + tableName);
		preparedStatement.execute();
		preparedStatement.close();
	}
	
	@Override
	protected void differentiatedUpdate(Connection connection, TableMetaData<?> tableMetaData) throws SQLException
	{
		// 无需特异性更新
	}
	
	@Override
	protected boolean checkColumnDefinitionFit(Connection connection, MapColumn each, TableMetaData<?> tableMetaData) throws SQLException
	{
		String sql = StringUtil.format("SELECT DATA_TYPE,DATA_LENGTH FROM SYS.USER_TAB_COLUMNS WHERE TABLE_NAME='{}' AND COLUMN_NAME='{}'", tableMetaData.getTableName(), each.getColName());
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		ResultSet executeQuery = preparedStatement.executeQuery();
		executeQuery.next();
		String data_type = executeQuery.getString(1);
		String data_length = executeQuery.getString(2);
		ColumnType columnType = each.getColumnType();
		preparedStatement.close();
		return data_type.equalsIgnoreCase(columnType.type()) && data_length.equalsIgnoreCase(columnType.desc());
	}
	
	@Override
	protected boolean columnExist(Connection connection, MapColumn each, TableMetaData<?> tableMetaData) throws SQLException
	{
		String sql = StringUtil.format("SELECT count(1) FROM SYS.USER_TAB_COLS WHERE TABLE_NAME='{}' AND COLUMN_NAME='{}'", tableMetaData.getTableName(), each.getColName());
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
		Comment comment = annotationUtil.getAnnotation(Comment.class, mapField.getField());
		if (comment == null)
		{
			return;
		}
		String sql = StringUtil.format("COMMENT ON COLUMN {}.{}.{} IS '{}'", schema, tableMetaData.getTableName(), mapField.getColName(), comment.value());
		PreparedStatement prepareStatement = connection.prepareStatement(sql);
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}
	
}
