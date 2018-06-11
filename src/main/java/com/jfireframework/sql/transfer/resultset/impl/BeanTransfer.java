package com.jfireframework.sql.transfer.resultset.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.metadata.TableEntityInfo;
import com.jfireframework.sql.metadata.TableEntityInfo.ColumnInfo;
import com.jfireframework.sql.transfer.column.ColumnTransfer;
import com.jfireframework.sql.transfer.column.ColumnTransfers;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

public class BeanTransfer implements ResultSetTransfer
{
	private Class<?>					ckass;
	private volatile ColumnTransfer[]	columnTransfers;
	
	@Override
	public Object transfer(ResultSet resultSet) throws SQLException
	{
		if (columnTransfers == null)
		{
			synchronized (ckass)
			{
				if (columnTransfers == null)
				{
					ResultSetMetaData metaData = resultSet.getMetaData();
					int columnCount = metaData.getColumnCount();
					ColumnTransfer[] transfers = new ColumnTransfer[columnCount];
					TableEntityInfo tableEntityInfo = TableEntityInfo.parse(ckass);
					for (int i = 0; i < columnCount; i++)
					{
						String columnName = metaData.getColumnName(i + 1);
						Field field = tableEntityInfo.getColumnInfoByColumnNameIgnoreCase(columnName).getField();
						transfers[i] = ColumnTransfers.parse(field, columnName);
					}
					this.columnTransfers = transfers;
				}
			}
		}
		try
		{
			Object entity = ckass.newInstance();
			for (ColumnTransfer each : columnTransfers)
			{
				each.setEntityValue(entity, resultSet);
			}
			return entity;
		}
		catch (Exception e)
		{
			throw new JustThrowException(e);
		}
	}
	
	@Override
	public ResultSetTransfer initialize(Class<?> type)
	{
		ckass = type;
		return this;
	}
	
	public void preSetColumnTransfer(List<String> properties, TableEntityInfo info)
	{
		Map<String, ColumnInfo> propertyNameKeyMap = info.getPropertyNameKeyMap();
		ColumnTransfer[] columnTransfers = new ColumnTransfer[properties.size()];
		int index = 0;
		for (String each : properties)
		{
			columnTransfers[index] = ColumnTransfers.parse(propertyNameKeyMap.get(each).getField(), propertyNameKeyMap.get(each).getColumnName());
			index++;
		}
		this.columnTransfers = columnTransfers;
	}
	
}
