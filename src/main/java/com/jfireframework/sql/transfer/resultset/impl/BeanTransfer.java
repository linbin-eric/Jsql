package com.jfireframework.sql.transfer.resultset.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.dbstructure.name.ColumnNameStrategy;
import com.jfireframework.sql.transfer.column.ColumnTransfer;
import com.jfireframework.sql.transfer.column.ColumnTransferDictionary;
import com.jfireframework.sql.util.ColumnHelper;

public class BeanTransfer<T> extends AbstractResultsetTransfer<T>
{
	private Map<String, ColumnTransfer>	columnNameDictory	= new HashMap<String, ColumnTransfer>();
	protected ColumnValueEntity[]		columnValueEntities;
	protected Class<T>					type;
	
	class ColumnValueEntity
	{
		ColumnTransfer	transfer;
		String			columnName;
	}
	
	@Override
	protected T valueOf(ResultSet resultSet) throws Exception
	{
		T entity = type.newInstance();
		for (ColumnValueEntity each : getColumns(resultSet))
		{
			each.transfer.setEntityValue(entity, each.columnName, resultSet);
		}
		return entity;
	}
	
	@SuppressWarnings("unchecked")
	private ColumnValueEntity[] getColumns(ResultSet resultSet)
	{
		if (columnValueEntities == null)
		{
			synchronized (BeanTransfer.this)
			{
				if (columnValueEntities == null)
				{
					try
					{
						ResultSetMetaData metaData = resultSet.getMetaData();
						int colCount = metaData.getColumnCount();
						List<ColumnValueEntity> resultFields = new LinkedList<ColumnValueEntity>();
						for (int i = 0; i < colCount; i++)
						{
							String columnName = metaData.getColumnName(i + 1).toLowerCase();
							ColumnTransfer fit = columnNameDictory.get(columnName);
							if (fit != null)
							{
								ColumnValueEntity entity = new ColumnValueEntity();
								entity.columnName = columnName;
								entity.transfer = fit;
								resultFields.add(entity);
							}
						}
						columnValueEntities = resultFields.toArray(new BeanTransfer.ColumnValueEntity[resultFields.size()]);
					}
					catch (SQLException e)
					{
						throw new JustThrowException(e);
					}
				}
			}
		}
		return columnValueEntities;
	}
	
	@Override
	public void initialize(Class<T> type, SessionfactoryConfig config)
	{
		ColumnTransferDictionary fieldOperatorDictionary = config.getFieldOperatorDictionary();
		this.type = type;
		ColumnNameStrategy columnNameStrategy = ColumnHelper.columnNameStrategy(type);
		for (Field each : ReflectUtil.getAllFields(type))
		{
			if (ColumnHelper.notColumnField(each))
			{
				continue;
			}
			columnNameDictory.put(ColumnHelper.columnName(each, columnNameStrategy).toLowerCase(), ColumnHelper.getColumnTransfer(each, fieldOperatorDictionary));
		}
	}
	
}
