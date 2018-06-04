package com.jfireframework.sql.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.annotation.ColumnNameStrategyDefinition;
import com.jfireframework.sql.annotation.Pk;
import com.jfireframework.sql.annotation.SqlIgnore;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.dbstructure.column.MapColumn;
import com.jfireframework.sql.dbstructure.column.impl.MapColumnImpl;
import com.jfireframework.sql.dbstructure.name.ColumnNameStrategy;
import com.jfireframework.sql.dbstructure.name.DefaultNameStrategy;

public class TableMetaData<T>
{
	private String					tableName;
	private Map<String, MapColumn>	allColumns	= new HashMap<String, MapColumn>();
	private MapColumn[]				valueColumns;
	private MapColumn				pkColumn;
	private Class<T>				ckass;
	private ColumnNameStrategy		colNameStrategy;
	private boolean					editable;
	
	public TableMetaData(Class<T> ckass, SessionfactoryConfig config)
	{
		this.ckass = ckass;
		TableEntity entity = ckass.getAnnotation(TableEntity.class);
		editable = entity.editable();
		switch (config.getTableNameCaseStrategy())
		{
			case UPPER:
				tableName = entity.name().toUpperCase();
				break;
			case LOWER:
				tableName = entity.name().toLowerCase();
				break;
			case ORIGIN:
				tableName = entity.name();
				break;
			default:
				tableName = entity.name();
		}
		try
		{
			Class<? extends ColumnNameStrategy> columnNameStrategyClass = ckass.isAnnotationPresent(ColumnNameStrategyDefinition.class) ? ckass.getAnnotation(ColumnNameStrategyDefinition.class).value() : DefaultNameStrategy.class;
			colNameStrategy = columnNameStrategyClass.newInstance();
		}
		catch (Exception e)
		{
			throw new JustThrowException(e);
		}
		List<MapColumn> list = new LinkedList<MapColumn>();
		for (Field each : ReflectUtil.getAllFields(ckass))
		{
			if (notTableField(each))
			{
				continue;
			}
			MapColumn mapField = new MapColumnImpl();
			mapField.initialize(each, colNameStrategy, config.getColumnTypeDictionary());
			allColumns.put(mapField.getFieldName(), mapField);
			if (each.isAnnotationPresent(Pk.class))
			{
				pkColumn = mapField;
				continue;
			}
			list.add(mapField);
		}
		valueColumns = list.toArray(new MapColumn[list.size()]);
		if (pkColumn != null && pkColumn.getField().getType().isPrimitive())
		{
			throw new IllegalArgumentException("作为主键的属性不可以使用基本类型，必须使用包装类。请检查" + pkColumn.getField().getDeclaringClass().getName() + "." + pkColumn.getField().getName());
		}
		list.remove(pkColumn);
		valueColumns = list.toArray(new MapColumn[list.size()]);
		
	}
	
	public boolean editable()
	{
		return editable;
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
	
	public ColumnNameStrategy getColNameStrategy()
	{
		return colNameStrategy;
	}
	
	public String getTableName()
	{
		return tableName;
	}
	
	public Class<T> getEntityClass()
	{
		return ckass;
	}
	
	public Map<String, MapColumn> getAllColumns()
	{
		return Collections.unmodifiableMap(allColumns);
	}
	
	public MapColumn[] getValueColumns()
	{
		return valueColumns;
	}
	
	public MapColumn getPkColumn()
	{
		return pkColumn;
	}
	
	public MapColumn getColumn(String name)
	{
		return allColumns.get(name);
	}
	
}
