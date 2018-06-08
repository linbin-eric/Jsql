package com.jfireframework.sql.dbstructure.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.pkstrategy.AutoIncrement;
import com.jfireframework.sql.dbstructure.Comment;
import com.jfireframework.sql.dbstructure.Index;
import com.jfireframework.sql.dbstructure.Structure;
import com.jfireframework.sql.dbstructure.TableDef;
import com.jfireframework.sql.dbstructure.column.Constraint;
import com.jfireframework.sql.dbstructure.column.MysqlColumnDef;
import com.jfireframework.sql.util.TableEntityInfo;

public class MysqlStructure implements Structure
{
	private String				findDatabase	= "select database()";
	private String				findTable		= "SELECT count(*) from information_schema.TABLES where TABLE_SCHEMA=? and TABLE_NAME=?";
	private String				findColumn		= "SELECT COLUMN_NAME,DATA_TYPE,IS_NULLABLE,CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION,NUMERIC_SCALE,COLUMN_COMMENT from information_schema.`COLUMNS` where TABLE_SCHEMA=? and TABLE_NAME=? and COLUMN_NAME=?";
	private String				addColumn		= "ALTER TABLE {}.{} add {}  ";
	private String				findColumnNames	= "SELECT COLUMN_NAME from information_schema.`COLUMNS` where TABLE_SCHEMA=? and TABLE_NAME=?";
	private static final Logger	logger			= LoggerFactory.getLogger(MysqlStructure.class);
	
	@Override
	public void createTable(DataSource dataSource, Set<TableEntityInfo> tableEntityInfos) throws SQLException
	{
		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(false);
		for (TableEntityInfo each : tableEntityInfos)
		{
			_createTable(each, connection);
		}
		connection.commit();
		connection.setAutoCommit(true);
	}
	
	private void _createTable(TableEntityInfo tableEntityInfo, Connection connection) throws SQLException
	{
		Class<?> entityClass = tableEntityInfo.getEntityClass();
		if (entityClass.isAnnotationPresent(TableDef.class) == false)
		{
			return;
		}
		Map<String, String> propertyNameToColumnNameMap = tableEntityInfo.getPropertyNameToColumnNameMap();
		TableDef tableDef = entityClass.getAnnotation(TableDef.class);
		String tableName = tableDef.tableName();
		dropTableIfExist(connection, tableName);
		createTable(connection, tableEntityInfo, propertyNameToColumnNameMap, tableDef, tableName);
	}
	
	private void createTable(Connection connection, TableEntityInfo info, Map<String, String> propertyNameToColumnNameMap, TableDef tableDef, String tableName) throws SQLException
	{
		String createTableSql = generateCreateTableSql(info, propertyNameToColumnNameMap, tableDef, tableName);
		logger.debug("traceId:{} 生成的建表语句是:{}", TRACEID.currentTraceId(), createTableSql);
		PreparedStatement prepareStatement = connection.prepareStatement(createTableSql);
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}
	
	private String generateCreateTableSql(TableEntityInfo each, Map<String, String> propertyNameToColumnNameMap, TableDef tableDef, String tableName)
	{
		StringCache cache = new StringCache();
		cache.append("CREATE TABLE '").append(tableName).append("' (\r\n");
		createBody(each, propertyNameToColumnNameMap, cache);
		for (Field field : each.getColumnNameToFieldMap().values())
		{
			MysqlColumnDef mysqlColumnDef = field.getAnnotation(MysqlColumnDef.class);
			String columnName = decideColumnName(propertyNameToColumnNameMap, field, mysqlColumnDef);
			if (field.isAnnotationPresent(Constraint.class))
			{
				setConstraint(cache, field, columnName);
			}
			if (field.isAnnotationPresent(Index.class))
			{
				setIndex(cache, field, columnName);
			}
		}
		if (cache.isCommaLast())
		{
			cache.deleteLast();
		}
		cache.append(") ENGINE = InnoDB DEFAULT CHARSET = utf8 ");
		if (StringUtil.isNotBlank(tableDef.comment()))
		{
			cache.append("comment='").append(tableDef.comment()).append("'");
		}
		return cache.toString();
	}
	
	private void setIndex(StringCache cache, Field field, String columnName)
	{
		Index index = field.getAnnotation(Index.class);
		String indexName = StringUtil.isNotBlank(index.indexName()) ? index.indexName() : columnName + "_idx_" + count.getAndIncrement();
		cache.append("KEY '").append(indexName).append("' ('").append(columnName).append("') USING ").append(index.indexType()).append(",\r\n");
	}
	
	private void setConstraint(StringCache cache, Field field, String columnName)
	{
		Constraint constraints = field.getAnnotation(Constraint.class);
		switch (constraints.type())
		{
			case PRIMARY_KEY:
				cache.append("PRIMARY KEY ('").append(columnName).append("'),\r\n");
				break;
			case UNIQUE_KEY:
				String constraintName = StringUtil.isNotBlank(constraints.name()) ? constraints.name() : columnName + "_uni_" + count.getAndIncrement();
				cache.append("UNIQUE KEY '").append(constraintName).append("' ('").append(columnName).append("'),\r\n");
				break;
			default:
				break;
		}
	}
	
	private void createBody(TableEntityInfo each, Map<String, String> propertyNameToColumnNameMap, StringCache cache)
	{
		for (Field field : each.getColumnNameToFieldMap().values())
		{
			MysqlColumnDef mysqlColumnDef = field.getAnnotation(MysqlColumnDef.class);
			String columnName = decideColumnName(propertyNameToColumnNameMap, field, mysqlColumnDef);
			cache.append('\'').append(columnName).append("' ");
			String columnType = decideColumnType(field, mysqlColumnDef);
			cache.append(columnType).append(' ');
			boolean isNullable = mysqlColumnDef != null ? mysqlColumnDef.isNullable() : false;
			if (isNullable == false)
			{
				cache.append("NOT NULL ");
			}
			if (field.isAnnotationPresent(AutoIncrement.class))
			{
				cache.append("AUTO_INCREMENT ");
			}
			if (field.isAnnotationPresent(Comment.class))
			{
				Comment comment = field.getAnnotation(Comment.class);
				cache.append(" COMMENT '").append(comment.value()).append('\'');
			}
			cache.appendComma().append("\r\n");
		}
	}
	
	private String decideColumnName(Map<String, String> propertyNameToColumnNameMap, Field field, MysqlColumnDef mysqlColumnDef)
	{
		String columnName = mysqlColumnDef != null ? //
		        StringUtil.isNotBlank(mysqlColumnDef.columnName()) ? mysqlColumnDef.columnName() : propertyNameToColumnNameMap.get(field.getName())//
		        : propertyNameToColumnNameMap.get(field.getName());
		return columnName;
	}
	
	private String decideColumnType(Field field, MysqlColumnDef mysqlColumnDef)
	{
		String columnType;
		if (mysqlColumnDef != null && StringUtil.isNotBlank(mysqlColumnDef.dataType()))
		{
			String dataType = mysqlColumnDef.dataType();
			if ("varchar".equals(dataType))
			{
				columnType = "varchar(" + mysqlColumnDef.maxCharacterLength() + ")";
			}
			else if ("float".equals(dataType) || "double".equals(dataType))
			{
				columnType = dataType + "(" + mysqlColumnDef.numeric_precision() + "," + mysqlColumnDef.numeric_scale() + ")";
			}
			else
			{
				columnType = dataType;
			}
		}
		else
		{
			Class<?> type = field.getType();
			if (type == String.class)
			{
				columnType = "varchar(64)";
			}
			else if (type == Integer.class || type == int.class || type == short.class || type == Short.class || type == byte.class || type == Byte.class)
			{
				columnType = "int";
			}
			else if (type == boolean.class || type == Boolean.class)
			{
				columnType = "tinyint";
			}
			else if (type == long.class || type == Long.class)
			{
				columnType = "bigint";
			}
			else if (type == float.class || type == Float.class || type == Double.class || type == double.class)
			{
				columnType = "double";
			}
			else
			{
				throw new UnsupportedOperationException("不支持的自动映射类型，请为属性" + field.getDeclaringClass().getName() + "." + field.getName() + "增加类定义注解");
			}
		}
		return columnType;
	}
	
	private void dropTableIfExist(Connection connection, String tableName) throws SQLException
	{
		PreparedStatement prepareStatement = connection.prepareStatement("DROP TABLE IF EXISTS " + tableName);
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}
	
	@Override
	public void updateTable(DataSource dataSource, Set<TableEntityInfo> tableEntityInfos) throws SQLException
	{
		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(false);
		String schema = getSchema(connection);
		for (TableEntityInfo each : tableEntityInfos)
		{
			Class<?> entityClass = each.getEntityClass();
			if (entityClass.isAnnotationPresent(TableDef.class) == false)
			{
				continue;
			}
			if (isTableExist(connection, schema, entityClass.getAnnotation(TableDef.class).tableName()) == false)
			{
				_createTable(each, connection);
				continue;
			}
			String tableName = entityClass.getAnnotation(TableDef.class).tableName();
			Map<String, String> propertyNameToColumnNameMap = each.getPropertyNameToColumnNameMap();
			addMissingColumns(connection, schema, each, tableName, propertyNameToColumnNameMap);
			dropNotExistColumns(connection, schema, tableName, each);
		}
	}
	
	class ConstraintInfo
	{
		String	constraintName;
		String	constraintType;
	}
	
	private void dropNotExistColumns(Connection connection, String schema, String tableName, TableEntityInfo info) throws SQLException
	{
		PreparedStatement prepareStatement = connection.prepareStatement(findColumnNames);
		prepareStatement.setString(1, schema);
		prepareStatement.setString(2, tableName);
		ResultSet resultSet = prepareStatement.executeQuery();
		List<String> notExistColumnNames = new LinkedList<String>();
		while (resultSet.next())
		{
			String columnName = resultSet.getString(1);
			if (info.getColumnNameToFieldMap().containsKey(columnName) == false)
			{
				notExistColumnNames.add(columnName);
			}
		}
		resultSet.close();
		prepareStatement.close();
		String dropColumnSqlPrefix = "ALTER TABLE " + tableName + " DROP COLUMN ";
		for (String notExistColumnName : notExistColumnNames)
		{
			PreparedStatement prepareStatement2 = connection.prepareStatement(dropColumnSqlPrefix + notExistColumnName);
			prepareStatement2.execute();
			prepareStatement2.close();
		}
	}
	
	private void addMissingColumns(Connection connection, String schema, TableEntityInfo each, String tableName, Map<String, String> propertyNameToColumnNameMap) throws SQLException
	{
		for (Field field : each.getColumnNameToFieldMap().values())
		{
			MysqlColumnDef mysqlColumnDef = field.getAnnotation(MysqlColumnDef.class);
			String columnName = decideColumnName(propertyNameToColumnNameMap, field, mysqlColumnDef);
			ColumnInfo columnInfo = queryColumnInfo(connection, schema, tableName, columnName);
			if (columnInfo == null)
			{
				addColumn(connection, schema, tableName, field, mysqlColumnDef, columnName);
			}
		}
	}
	
	private void addColumn(Connection connection, String schema, String tableName, Field field, MysqlColumnDef mysqlColumnDef, String columnName) throws SQLException
	{
		StringCache cache = new StringCache(StringUtil.format(addColumn, schema, tableName, columnName));
		String columnType = decideColumnType(field, mysqlColumnDef);
		cache.append(columnType).append(' ');
		boolean isNullable = mysqlColumnDef != null ? mysqlColumnDef.isNullable() : false;
		if (isNullable == false)
		{
			cache.append("NOT NULL ");
		}
		if (field.isAnnotationPresent(AutoIncrement.class))
		{
			cache.append("AUTO_INCREMENT ");
		}
		if (field.isAnnotationPresent(Comment.class))
		{
			Comment comment = field.getAnnotation(Comment.class);
			cache.append(" COMMENT '").append(comment.value()).append('\'');
		}
		String addColumnSql = cache.toString();
		PreparedStatement prepareStatement = connection.prepareStatement(addColumnSql);
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}
	
	class ColumnInfo
	{
		String	columnName;
		String	dataType;
		boolean	isNullable;
		Integer	maxCharactersLength;
		String	comment;
		int		numeric_precision;
		int		numeric_scale;
	}
	
	private ColumnInfo queryColumnInfo(Connection connection, String schema, String tableName, String columnName) throws SQLException
	{
		PreparedStatement prepareStatement = connection.prepareStatement(findColumn);
		prepareStatement.setString(1, schema);
		prepareStatement.setString(2, tableName);
		prepareStatement.setString(3, columnName);
		ResultSet resultSet = prepareStatement.executeQuery();
		if (resultSet.next() == false)
		{
			return null;
		}
		else
		{
			ColumnInfo columnInfo = new ColumnInfo();
			columnInfo.columnName = resultSet.getString("COLUMN_NAME");
			columnInfo.dataType = resultSet.getString("DATA_TYPE");
			columnInfo.isNullable = resultSet.getString("IS_NULLABLE").equals("YES");
			int CHARACTER_MAXIMUM_LENGTH = resultSet.getInt("CHARACTER_MAXIMUM_LENGTH");
			columnInfo.maxCharactersLength = resultSet.wasNull() ? null : CHARACTER_MAXIMUM_LENGTH;
			int NUMERIC_PRECISION = resultSet.getInt("NUMERIC_PRECISION");
			columnInfo.numeric_precision = resultSet.wasNull() ? null : NUMERIC_PRECISION;
			int NUMERIC_SCALE = resultSet.getInt("NUMERIC_SCALE");
			columnInfo.numeric_scale = resultSet.wasNull() ? null : NUMERIC_SCALE;
			columnInfo.comment = resultSet.getString("COLUMN_COMMENT");
			return columnInfo;
		}
	}
	
	private boolean isTableExist(Connection connection, String schema, String tableName) throws SQLException
	{
		PreparedStatement prepareStatement = connection.prepareStatement(findTable);
		prepareStatement.setString(1, schema);
		prepareStatement.setString(2, tableName);
		ResultSet resultSet = prepareStatement.executeQuery();
		resultSet.next();
		int count = resultSet.getInt(1);
		return count == 1;
	}
	
	private String getSchema(Connection connection) throws SQLException
	{
		PreparedStatement prepareStatement = connection.prepareStatement(findDatabase);
		ResultSet resultSet = prepareStatement.executeQuery();
		resultSet.next();
		String schema = resultSet.getString(1);
		return schema;
	}
	
}
