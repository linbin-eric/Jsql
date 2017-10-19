package com.jfireframework.sql.dbstructure.impl;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.dbstructure.Structure;
import com.jfireframework.sql.dbstructure.column.ColumnType;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.metadata.TableMetaData;

public abstract class AbstractDBStructure implements Structure
{
	protected static final Logger	logger	= LoggerFactory.getLogger(Structure.class);
	protected final String			schema;
	
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
				_createTable(connection, metaData);
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
	
	protected String getDesc(MapField fieldInfo, TableMetaData tableMetaData)
	{
		ColumnType columnType = tableMetaData.columnType(fieldInfo);
		if (StringUtil.isNotBlank(columnType.desc()))
		{
			return columnType.type() + "(" + columnType.desc() + ")";
		}
		else
		{
			return columnType.type();
		}
	}
	
	protected abstract void _createTable(Connection connection, TableMetaData tableMetaData) throws SQLException;
	
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
					_updateTable(connection, metaData);
				}
				else
				{
					_createTable(connection, metaData);
				}
			}
			connection.commit();
		}
		finally
		{
			if (connection != null)
			{
				connection.close();
			}
		}
	}
	
	protected abstract boolean checkIfTableExists(Connection connection, TableMetaData metaData) throws SQLException;
	
	protected abstract void _updateTable(Connection connection, TableMetaData tableMetaData) throws SQLException;
}
