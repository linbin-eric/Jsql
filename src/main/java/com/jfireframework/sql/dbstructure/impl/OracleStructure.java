package com.jfireframework.sql.dbstructure.impl;

import java.sql.Connection;
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
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.metadata.TableMetaData;

public class OracleStructure extends AbstractDBStructure
{
	public OracleStructure(String schema)
	{
		super(schema.toUpperCase());
	}
	
	private static final Logger logger = LoggerFactory.getLogger(OracleStructure.class);
	
	protected boolean checkIfTableExists(Connection connection, TableMetaData metaData) throws SQLException
	{
		String tableName = metaData.getTableName();
		ResultSet resultSet = connection.prepareStatement(StringUtil.format("SELECT * FROM SYS.USER_TABLES t WHERE t.TABLE_NAME='{}'", tableName)).executeQuery();
		return resultSet.next();
	}
	
	protected void _updateTable(Connection connection, TableMetaData tableMetaData) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String tableName = tableMetaData.getTableName();
		deletePkConstraint(connection, tableName);
		String queryColumnSqlTemplate = "SELECT t.COLUMN_NAME,t.DATA_TYPE,c.COMMENTS FROM user_tab_columns t,user_col_comments c WHERE t.table_name = c.table_name AND t.column_name = c.column_name AND t.table_name ='{}' AND t.COLUMN_NAME = '{}'";
		for (MapField each : tableMetaData.getFieldInfos())
		{
			String sql = StringUtil.format(queryColumnSqlTemplate, tableName, each.getColName());
			logger.trace("traceId:{} 查询表:{}的列:{}的sql为{}", traceId, tableName, each.getColName(), sql);
			ResultSet executeQuery = connection.prepareStatement(sql).executeQuery();
			if (executeQuery.next())
			{
				logger.trace("traceId:{} 表:{}中的列:{}存在", traceId, tableName, each.getColName());
				String dataType = executeQuery.getString(2);
				ColumnType columnType = tableMetaData.columnType(each);
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
	
	private void deleteUnExistColumns(Connection connection, TableMetaData tableMetaData, String tableName) throws SQLException
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
	
	private void addPKConstraint(Connection connection, TableMetaData tableMetaData, String tableName) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String sql = StringUtil.format("ALTER TABLE {} ADD CONSTRAINT {} PRIMARY KEY ({})", tableName, tableMetaData.getIdInfo().getColName() + "_PK", tableMetaData.getIdInfo().getColName());
		logger.debug("traceId:{} 准备增加主键约束，sql为:{}", traceId, sql);
		connection.prepareStatement(sql).executeUpdate();
	}
	
	private void addColumn(Connection connection, TableMetaData tableMetaData, String tableName, MapField each) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String sql = StringUtil.format("ALTER TABLE {} ADD {} {}", tableName, each.getColName(), getDesc(each, tableMetaData));
		logger.debug("traceId:{} 执行添加列语句:{}", traceId, sql);
		connection.prepareStatement(sql).executeUpdate();
	}
	
	private void updateColumn(Connection connection, TableMetaData tableMetaData, String tableName, MapField each) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String sql = StringUtil.format("ALTER TABLE {} MODIFY {} {}", tableName, each.getColName(), getDesc(each, tableMetaData));
		logger.debug("traceId:{} 执行执行的更新语句是:{}", traceId, sql);
		connection.prepareStatement(sql).executeUpdate();
	}
	
	private void deletePkConstraint(Connection connection, String tableName) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String findPkSql = "select cu.constraint_name from user_cons_columns cu, user_constraints au where cu.constraint_name = au.constraint_name and au.constraint_type = 'P' and au.table_name = '" + tableName + "'";
		ResultSet resultSet = connection.prepareStatement(findPkSql).executeQuery();
		resultSet.next();
		String pkName = resultSet.getString(1);
		resultSet.close();
		logger.trace("traceId:{} 查询到表:{}的主键约束名称:{}", traceId, tableName, pkName);
		String deletePkSql = "ALTER TABLE " + tableName + " DROP CONSTRAINT " + pkName;
		logger.trace("traceId:{} 准备删除主键约束，执行:{}", traceId, deletePkSql);
		connection.prepareStatement(deletePkSql).executeUpdate();
		logger.trace("traceId:{} 主键约束删除完毕", traceId);
	}
	
	@Override
	protected void _createTable(Connection connection, TableMetaData tableMetaData) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String tableName = tableMetaData.getTableName();
		MapField idInfo = tableMetaData.getIdInfo();
		StringCache cache = new StringCache();
		cache.append("CREATE TABLE ").append(tableName).append(" ( ");
		cache.append(idInfo.getColName()).append(' ').append(getDesc(idInfo, tableMetaData)).appendComma();
		for (MapField each : tableMetaData.getFieldInfos())
		{
			if (each.getFieldName().equals(idInfo.getFieldName()))
			{
				continue;
			}
			try
			{
				cache.append(" ").append(each.getColName()).append(' ').append(getDesc(each, tableMetaData)).appendComma();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		cache.append(" constraint ").append(tableMetaData.getIdInfo().getColName()).append("_PK").append(" primary key (").append(idInfo.getColName()).append(")");
		cache.append(")");
		String sql = cache.toString();
		if (checkIfTableExists(connection, tableMetaData))
		{
			logger.debug("traceId:{} 表:{}已经存在，先进行删除", traceId, tableName);
			connection.prepareStatement("DROP TABLE " + tableName).execute();
		}
		logger.warn("进行表:{}的创建，创建语句是\n{}", tableName, sql);
		connection.prepareStatement(sql).execute();
	}
	
}
