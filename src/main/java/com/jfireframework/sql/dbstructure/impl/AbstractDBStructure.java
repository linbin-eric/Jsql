package com.jfireframework.sql.dbstructure.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.annotation.Column;
import com.jfireframework.sql.dbstructure.Structure;
import com.jfireframework.sql.dbstructure.column.ColumnType;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.metadata.TableMetaData;

public abstract class AbstractDBStructure implements Structure
{
	protected static final Logger	logger			= LoggerFactory.getLogger(Structure.class);
	protected final String			schema;
	protected final AnnotationUtil	annotationUtil	= new AnnotationUtil();
	
	public AbstractDBStructure(String schema)
	{
		this.schema = schema;
	}
	
	@Override
	public void createTable(DataSource dataSource, TableMetaData[] metaDatas) throws SQLException
	{
		Connection connection = null;
		try
		{
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
			for (TableMetaData metaData : metaDatas)
			{
				if (metaData.getIdInfo() == null || metaData.editable() == false)
				{
					continue;
				}
				createTable(connection, metaData);
			}
			connection.commit();
		}
		catch (Exception e)
		{
			throw new JustThrowException(e);
		}
		finally
		{
			if (connection != null)
			{
				connection.close();
			}
		}
		
	}
	
	protected void createTable(Connection connection, TableMetaData tableMetaData) throws SQLException
	{
		if (checkIfTableExists(connection, tableMetaData))
		{
			deleteExistTable(connection, tableMetaData);
		}
		execCreateTable(connection, tableMetaData);
		setComments(connection, tableMetaData);
		differentiatedUpdate(connection, tableMetaData);
	}
	
	private void setComments(Connection connection, TableMetaData tableMetaData) throws SQLException
	{
		logger.debug("traceId:{} 准备进行注释语句的添加", TRACEID.currentTraceId());
		for (MapField mapField : tableMetaData.getFieldInfos())
		{
			setComment(mapField, tableMetaData, connection);
		}
	}
	
	protected abstract void setComment(MapField mapField, TableMetaData tableMetaData, Connection connection) throws SQLException;
	
	private void execCreateTable(Connection connection, TableMetaData tableMetaData) throws SQLException
	{
		String sql = buildCreateTableSql(tableMetaData);
		logger.debug("traceId:{} 进行表:{}的创建，创建语句是:{}", TRACEID.currentTraceId(), tableMetaData.getTableName(), sql);
		connection.prepareStatement(sql).execute();
	}
	
	/**
	 * 在创建表的时候执行一些差异性的更新操作
	 * 
	 * @param connection
	 * @param tableMetaData
	 * @throws SQLException
	 */
	protected abstract void differentiatedUpdate(Connection connection, TableMetaData tableMetaData) throws SQLException;
	
	protected abstract String buildCreateTableSql(TableMetaData tableMetaData);
	
	protected abstract void deleteExistTable(Connection connection, TableMetaData metaData) throws SQLException;
	
	protected String getDesc(MapField fieldInfo, TableMetaData tableMetaData)
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
		Field f = fieldInfo.getField();
		if (f.isAnnotationPresent(Column.class) && f.getAnnotation(Column.class).nullable() == false)
		{
			cache.append(" NOT NULL");
		}
		return cache.toString();
	}
	
	@Override
	public void updateTable(DataSource dataSource, TableMetaData[] metaDatas) throws SQLException
	{
		Connection connection = null;
		try
		{
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
			for (TableMetaData metaData : metaDatas)
			{
				if (checkIfTableExists(connection, metaData))
				{
					updateTable(connection, metaData);
				}
				else
				{
					createTable(connection, metaData);
				}
			}
			connection.commit();
		}
		finally
		{
			if (connection != null)
			{
				connection.rollback();
				connection.close();
			}
		}
	}
	
	protected abstract boolean checkIfTableExists(Connection connection, TableMetaData metaData) throws SQLException;
	
	protected void updateTable(Connection connection, TableMetaData tableMetaData) throws SQLException
	{
		String traceId = TRACEID.currentTraceId();
		String tableName = tableMetaData.getTableName();
		deletePkConstraint(connection, tableMetaData);
		for (MapField each : tableMetaData.getFieldInfos())
		{
			if (columnExist(connection, each, tableMetaData))
			{
				if (checkColumnDefinitionFit(connection, each, tableMetaData) == false)
				{
					logger.debug("traceId:{} 表:{}中的列:{}与类:{}的字段类型不符合，需要更新", traceId, tableName, each.getColName(), each.getField().getDeclaringClass().getSimpleName());
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
		differentiatedUpdate(connection, tableMetaData);
	}
	
	protected abstract boolean columnExist(Connection connection, MapField each, TableMetaData tableMetaData) throws SQLException;
	
	protected abstract boolean checkColumnDefinitionFit(Connection connection, MapField each, TableMetaData tableMetaData) throws SQLException;
	
	protected abstract void updateColumn(Connection connection, TableMetaData tableMetaData, String tableName, MapField each) throws SQLException;
	
	protected abstract void deletePkConstraint(Connection connection, TableMetaData tableMetaData) throws SQLException;
	
	protected abstract void addColumn(Connection connection, TableMetaData tableMetaData, String tableName, MapField each) throws SQLException;
	
	protected abstract void addPKConstraint(Connection connection, TableMetaData tableMetaData, String tableName) throws SQLException;
	
	protected abstract void deleteUnExistColumns(Connection connection, TableMetaData tableMetaData, String tableName) throws SQLException;
	
}
