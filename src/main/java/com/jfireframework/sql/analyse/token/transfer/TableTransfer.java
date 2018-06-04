package com.jfireframework.sql.analyse.token.transfer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.Column;
import com.jfireframework.sql.annotation.ColumnNameStrategyDefinition;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.dbstructure.name.ColumnNameStrategy;
import com.jfireframework.sql.dbstructure.name.DefaultLowerCaseNameStrategy;

public class TableTransfer
{
	private String							className;
	private String							classSimpleName;
	private String							tableName;
	private Map<String, String>				propertyNameToColumnNameMap	= new HashMap<String, String>();
	private static final ColumnNameStrategy	defaultStrategy				= new DefaultLowerCaseNameStrategy();
	
	public TableTransfer(Class<?> ckass)
	{
		className = ckass.getName();
		classSimpleName = ckass.getName();
		tableName = ckass.getAnnotation(TableEntity.class).name();
		try
		{
			ColumnNameStrategy strategy = ckass.isAnnotationPresent(ColumnNameStrategyDefinition.class) ? //
			        ckass.getAnnotation(ColumnNameStrategyDefinition.class).value().newInstance()//
			        : defaultStrategy;
			for (Field field : ReflectUtil.getAllFields(ckass))
			{
				if (field.isAnnotationPresent(Column.class) && StringUtil.isNotBlank(field.getAnnotation(Column.class).name()))
				{
					propertyNameToColumnNameMap.put(field.getName(), field.getAnnotation(Column.class).name());
					continue;
				}
				propertyNameToColumnNameMap.put(field.getName(), strategy.toColumnName(field.getName()));
			}
		}
		catch (Exception e)
		{
			throw new JustThrowException(e);
		}
	}
	
	public String getClassSimpleName()
	{
		return classSimpleName;
	}
	
	public String getTableName()
	{
		return tableName;
	}
	
	public Map<String, String> getPropertyNameToColumnNameMap()
	{
		return propertyNameToColumnNameMap;
	}
	
	@Override
	public String toString()
	{
		return "TableTransfer [className=" + className + ", tableName=" + tableName + "]";
	}
	
}
