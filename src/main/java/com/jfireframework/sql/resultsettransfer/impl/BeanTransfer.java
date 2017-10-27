package com.jfireframework.sql.resultsettransfer.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Blob;
import java.sql.Clob;
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
import com.jfireframework.sql.annotation.NameStrategy;
import com.jfireframework.sql.annotation.SqlIgnore;
import com.jfireframework.sql.dbstructure.name.ColumnNameStrategy;
import com.jfireframework.sql.dbstructure.name.DefaultNameStrategy;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.mapfield.impl.MapFieldImpl;

public class BeanTransfer extends AbstractResultsetTransfer
{
	private Map<String, MapField>	columnNameDictory	= new HashMap<String, MapField>();
	protected MapField[]			columns;
	protected Class<?>				type;
	
	@Override
	protected Object valueOf(ResultSet resultSet) throws Exception
	{
		Object entity = type.newInstance();
		for (MapField each : getColumns(resultSet))
		{
			each.setEntityValue(entity, resultSet);
		}
		return entity;
	}
	
	private MapField[] getColumns(ResultSet resultSet)
	{
		if (columns == null)
		{
			synchronized (BeanTransfer.this)
			{
				if (columns == null)
				{
					try
					{
						ResultSetMetaData metaData = resultSet.getMetaData();
						int colCount = metaData.getColumnCount();
						List<MapField> resultFields = new LinkedList<MapField>();
						for (int i = 0; i < colCount; i++)
						{
							MapField fit = columnNameDictory.get(metaData.getColumnName(i + 1).toLowerCase());
							if (fit != null)
							{
								resultFields.add(fit);
							}
						}
						columns = resultFields.toArray(new MapField[resultFields.size()]);
					}
					catch (SQLException e)
					{
						throw new JustThrowException(e);
					}
				}
			}
		}
		return columns;
	}
	
	@Override
	public void initialize(Class<?> type, SessionfactoryConfig config)
	{
		this.type = type;
		ColumnNameStrategy colNameStrategy;
		try
		{
			Class<? extends ColumnNameStrategy> ckass = type.isAnnotationPresent(NameStrategy.class) ? type.getAnnotation(NameStrategy.class).value() : DefaultNameStrategy.class;
			colNameStrategy = ckass.newInstance();
		}
		catch (Exception e)
		{
			throw new JustThrowException(e);
		}
		for (Field each : ReflectUtil.getAllFields(type))
		{
			if (notTableField(each))
			{
				continue;
			}
			MapField mapField = new MapFieldImpl();
			mapField.initialize(each, colNameStrategy, config.getFieldOperatorDictionary(), config.getColumnTypeDictionary());
			columnNameDictory.put(mapField.getColName().toLowerCase(), mapField);
		}
	}
	
	private boolean notTableField(Field field)
	{
		if (field.getType() == Clob.class || field.getType() == Blob.class)
		{
			return false;
		}
		if (field.isAnnotationPresent(SqlIgnore.class) //
		        || Map.class.isAssignableFrom(field.getType())//
		        || List.class.isAssignableFrom(field.getType())//
		        || (field.getType().isInterface())//
		        || Modifier.isStatic(field.getModifiers()))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
