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
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.metadata.TableMetaData;

public class OracleStructure extends AbstractDBStructure
{
	public OracleStructure(String schema)
	{
		super(schema.toUpperCase());
	}
	
	private static final Logger logger = LoggerFactory.getLogger(OracleStructure.class);
	
	@Override
	protected boolean checkIfTableExists(Connection connection, TableMetaData metaData) throws SQLException
	{
		String tableName = metaData.getTableName();
		ResultSet resultSet = connection.prepareStatement(StringUtil.format("SELECT * FROM SYS.USER_TABLES t WHERE t.TABLE_NAME='{}'", tableName)).executeQuery();
		return resultSet.next();
	}
	
	@Override
	protected void deleteUnExistColumns(Connection connection, TableMetaData tableMetaData, String tableName) throws SQLException
	{
		Set<String> columnNames = new HashSet<String>();
		for (MapField each : tableMetaData.getFieldInfos())
		{
			columnNames.add(each.getColName());
		}
		String sql = StringUtil.format("SELECT t.COLUMN_NAME FROM user_tab_columns t WHERE t.TABLE_NAME='{}'", tableName);
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
	protected void addPKConstraint(Connection connection, TableMetaData tableMetaData, String tableName) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String sql = StringUtil.format("ALTER TABLE {} ADD CONSTRAINT {} PRIMARY KEY ({})", tableName, tableMetaData.getIdInfo().getColName() + "_PK", tableMetaData.getIdInfo().getColName());
		logger.debug("traceId:{} 准备增加主键约束，sql为:{}", traceId, sql);
		connection.prepareStatement(sql).executeUpdate();
	}
	
	@Override
	protected void addColumn(Connection connection, TableMetaData tableMetaData, String tableName, MapField each) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String sql = StringUtil.format("ALTER TABLE {} ADD {} {}", tableName, each.getColName(), getDesc(each, tableMetaData));
		logger.debug("traceId:{} 执行添加列语句:{}", traceId, sql);
		connection.prepareStatement(sql).executeUpdate();
	}
	
	@Override
	protected void updateColumn(Connection connection, TableMetaData tableMetaData, String tableName, MapField each) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String sql = StringUtil.format("ALTER TABLE {} MODIFY {} {}", tableName, each.getColName(), getDesc(each, tableMetaData));
		logger.debug("traceId:{} 执行执行的更新语句是:{}", traceId, sql);
		connection.prepareStatement(sql).executeUpdate();
	}
	
	@Override
	protected void deletePkConstraint(Connection connection, TableMetaData tableMetaData) throws SQLException
	{
		String tableName = tableMetaData.getTableName();
		String traceId = TRACEID.currentTraceId();
		String findPkSql = StringUtil.format("SELECT CONSTRAINT_NAME FROM SYS.USER_CONSTRAINTS WHERE TABLE_NAME='{}'", tableMetaData.getTableName());
		ResultSet resultSet = connection.prepareStatement(findPkSql).executeQuery();
		if (resultSet.next())
		{
			String pkName = resultSet.getString(1);
			resultSet.close();
			logger.trace("traceId:{} 查询到表:{}的主键约束名称:{}", traceId, tableName, pkName);
			String deletePkSql = "ALTER TABLE " + tableName + " DROP CONSTRAINT " + pkName;
			logger.trace("traceId:{} 准备删除主键约束，执行:{}", traceId, deletePkSql);
			connection.prepareStatement(deletePkSql).executeUpdate();
			logger.trace("traceId:{} 主键约束删除完毕", traceId);
		}
	}
	
	@Override
	protected String buildCreateTableSql(TableMetaData tableMetaData)
	{
		String tableName = schema + "." + tableMetaData.getTableName();
		StringCache cache = new StringCache();
		cache.append("CREATE TABLE ").append(tableName).append(" ( ");
		for (MapField each : tableMetaData.getFieldInfos())
		{
			cache.append(" ").append(each.getColName()).append(' ').append(getDesc(each, tableMetaData)).appendComma();
		}
		String pkName = StringUtil.format("PK_{}", tableName.hashCode() & 0x7fffffff);
		cache.append(" CONSTRAINT ").append(pkName).append(" PRIMARY KEY (").append(tableMetaData.getIdInfo().getColName()).append("))");
		return cache.toString();
	}
	
	@Override
	protected void deleteExistTable(Connection connection, TableMetaData metaData) throws SQLException
	{
		String tableName = schema + "." + metaData.getTableName();
		connection.prepareStatement("DROP TABLE " + tableName).execute();
	}
	
	@Override
	protected void differentiatedUpdate(Connection connection, TableMetaData tableMetaData) throws SQLException
	{
		// 无需特异性更新
	}
	
	@Override
	protected boolean checkColumnDefinitionFit(Connection connection, MapField each, TableMetaData tableMetaData) throws SQLException
	{
		String sql = StringUtil.format("SELECT DATA_TYPE,DATA_LENGTH FROM SYS.USER_TAB_COLUMNS WHERE TABLE_NAME='{}' AND COLUMN_NAME='{}'", tableMetaData.getTableName(), each.getColName());
		ResultSet executeQuery = connection.prepareStatement(sql).executeQuery();
		executeQuery.next();
		String data_type = executeQuery.getString(1);
		String data_length = executeQuery.getString(2);
		ColumnType columnType = each.getColumnType();
		return data_type.equalsIgnoreCase(columnType.type()) && data_length.equalsIgnoreCase(columnType.desc());
	}
	
	@Override
	protected boolean columnExist(Connection connection, MapField each, TableMetaData tableMetaData) throws SQLException
	{
		String sql = StringUtil.format("SELECT count(1) FROM SYS.USER_TAB_COLS WHERE TABLE_NAME='{}' AND COLUMN_NAME='{}'", tableMetaData.getTableName(), each.getColName());
		ResultSet executeQuery = connection.prepareStatement(sql).executeQuery();
		executeQuery.next();
		return executeQuery.getInt(1) > 0;
	}
	
	@Override
	protected void setComment(MapField mapField, TableMetaData tableMetaData, Connection connection) throws SQLException
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
