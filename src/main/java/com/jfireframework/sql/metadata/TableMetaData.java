package com.jfireframework.sql.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Blob;
import java.sql.Clob;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.annotation.Pk;
import com.jfireframework.sql.annotation.SqlIgnore;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.mapfield.impl.MapFieldImpl;

public class TableMetaData
{
	private String			tableName;
	private MapField[]		fieldInfos;
	private MapField		idInfo;
	private Class<?>		ckass;
	private ColNameStrategy	colNameStrategy;
	private boolean			editable;
	
	public TableMetaData(Class<?> ckass, ColNameStrategy nameStrategy, SessionfactoryConfig config)
	{
		this.ckass = ckass;
		this.colNameStrategy = nameStrategy;
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
		List<MapField> list = new LinkedList<MapField>();
		for (Field each : ReflectUtil.getAllFields(ckass))
		{
			if (notTableField(each))
			{
				continue;
			}
			MapField mapField = new MapFieldImpl();
			mapField.initialize(each, nameStrategy, config.getFieldOperatorDictionary(), config.getJdbcTypeDictionary());
			list.add(mapField);
			if (each.isAnnotationPresent(Pk.class))
			{
				idInfo = mapField;
			}
		}
		fieldInfos = list.toArray(new MapField[list.size()]);
		if (idInfo.getField() != null && idInfo.getField().getType().isPrimitive())
		{
			throw new IllegalArgumentException("作为主键的属性不可以使用基本类型，必须使用包装类。请检查" + idInfo.getField().getDeclaringClass().getName() + "." + idInfo.getField().getName());
		}
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
	
	public ColNameStrategy getColNameStrategy()
	{
		return colNameStrategy;
	}
	
	public String getTableName()
	{
		return tableName;
	}
	
	public MapField[] getFieldInfos()
	{
		return fieldInfos;
	}
	
	public MapField getIdInfo()
	{
		return idInfo;
	}
	
	public Class<?> getEntityClass()
	{
		return ckass;
	}
	
}
