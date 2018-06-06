package com.jfireframework.sql.transfer.resultset.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.transfer.column.ColumnTransfer;
import com.jfireframework.sql.transfer.column.ColumnTransfers;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;
import com.jfireframework.sql.util.TableEntityInfo;

public class BeanTransfer implements ResultSetTransfer
{
	private Class<?>					ckass;
	private volatile ColumnTransfer[]	transfers;
	
	@Override
	public Object transfer(ResultSet resultSet) throws SQLException
	{
		if (transfers == null)
		{
			synchronized (ckass)
			{
				if (transfers == null)
				{
					ResultSetMetaData metaData = resultSet.getMetaData();
					int columnCount = metaData.getColumnCount();
					ColumnTransfer[] transfers = new ColumnTransfer[columnCount];
					Map<String, Field> columnNameToFieldMap = TableEntityInfo.parse(ckass).getColumnNameToFieldMap();
					for (int i = 0; i < columnCount; i++)
					{
						String columnName = metaData.getColumnName(i + 1);
						Field field = columnNameToFieldMap.get(columnName);
						transfers[i] = ColumnTransfers.parse(field, columnName);
					}
					this.transfers = transfers;
				}
			}
		}
		try
		{
			Object entity = ckass.newInstance();
			for (ColumnTransfer each : transfers)
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
	
}
