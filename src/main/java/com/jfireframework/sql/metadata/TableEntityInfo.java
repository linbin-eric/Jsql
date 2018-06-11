package com.jfireframework.sql.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.StandardColumnDef;
import com.jfireframework.sql.annotation.ColumnNameStrategyDef;
import com.jfireframework.sql.annotation.Pk;
import com.jfireframework.sql.annotation.SqlIgnore;
import com.jfireframework.sql.annotation.TableDef;

public class TableEntityInfo
{
	private static final Map<Class<?>, TableEntityInfo>	store	= new ConcurrentHashMap<Class<?>, TableEntityInfo>();
	
	private String										className;
	private String										classSimpleName;
	private String										tableName;
	private Map<String, ColumnInfo>						propertyNameKeyMap;
	private Map<String, ColumnInfo>						columnNameIgnoreCaseKeyMap;
	private ColumnInfo									pkInfo;
	private Class<?>									ckass;
	
	private TableEntityInfo(Class<?> ckass)
	{
		this.ckass = ckass;
		className = ckass.getName();
		classSimpleName = ckass.getName();
		tableName = ckass.getAnnotation(TableDef.class).name();
		Map<String, ColumnInfo> propertyNameKeyMap = new HashMap<String, TableEntityInfo.ColumnInfo>();
		Map<String, ColumnInfo> columnNameIgnoreCaseKeyMap = new HashMap<String, TableEntityInfo.ColumnInfo>();
		try
		{
			ColumnNameStrategy strategy = ckass.isAnnotationPresent(ColumnNameStrategyDef.class) ? //
			        ckass.getAnnotation(ColumnNameStrategyDef.class).value().newInstance()//
			        : DefaultLowerCaseNameStrategy.instance;
			for (Field field : ReflectUtil.getAllFields(ckass))
			{
				if (isNotColumnField(field))
				{
					continue;
				}
				field.setAccessible(true);
				String columnName = field.isAnnotationPresent(StandardColumnDef.class) && StringUtil.isNotBlank(field.getAnnotation(StandardColumnDef.class).columnName()) ? field.getAnnotation(StandardColumnDef.class).columnName() : strategy.toColumnName(field.getName());
				ColumnInfo columnInfo = new ColumnInfo();
				columnInfo.setColumnName(columnName);
				columnInfo.setField(field);
				columnInfo.setPropertyName(field.getName());
				propertyNameKeyMap.put(field.getName(), columnInfo);
				columnNameIgnoreCaseKeyMap.put(columnName.toLowerCase(), columnInfo);
				if (field.isAnnotationPresent(Pk.class))
				{
					if (pkInfo == null)
					{
						pkInfo = new ColumnInfo();
						pkInfo.setField(field);
						pkInfo.setColumnName(columnName);
						pkInfo.setPropertyName(field.getName());
					}
					else
					{
						throw new IllegalStateException("一个实体类不能注解两个PK注解，请检查" + field.getDeclaringClass().getName());
					}
				}
			}
			this.propertyNameKeyMap = Collections.unmodifiableMap(propertyNameKeyMap);
			this.columnNameIgnoreCaseKeyMap = Collections.unmodifiableMap(columnNameIgnoreCaseKeyMap);
		}
		catch (Exception e)
		{
			throw new JustThrowException(e);
		}
	}
	
	protected boolean isNotColumnField(Field field)
	{
		if (field.isAnnotationPresent(SqlIgnore.class))
		{
			return true;
		}
		int modifiers = field.getModifiers();
		if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers))
		{
			return true;
		}
		Class<?> type = field.getType();
		if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type))
		{
			return true;
		}
		return false;
	}
	
	public String getClassSimpleName()
	{
		return classSimpleName;
	}
	
	public String getTableName()
	{
		return tableName;
	}
	
	public Map<String, ColumnInfo> getPropertyNameKeyMap()
	{
		return propertyNameKeyMap;
	}
	
	public ColumnInfo getPkInfo()
	{
		return pkInfo;
	}
	
	@Override
	public String toString()
	{
		return "TableTransfer [className=" + className + ", tableName=" + tableName + "]";
	}
	
	public Class<?> getEntityClass()
	{
		return ckass;
	}
	
	public ColumnInfo getColumnInfoByColumnNameIgnoreCase(String columnName)
	{
		return columnNameIgnoreCaseKeyMap.get(columnName.toLowerCase());
	}
	
	public static TableEntityInfo parse(Class<?> entityClass)
	{
		TableEntityInfo tableEntityInfo = store.get(entityClass);
		if (tableEntityInfo == null)
		{
			tableEntityInfo = new TableEntityInfo(entityClass);
			store.put(entityClass, tableEntityInfo);
		}
		return tableEntityInfo;
	}
	
	public static class ColumnInfo
	{
		String	columnName;
		String	propertyName;
		Field	field;
		
		public String getColumnName()
		{
			return columnName;
		}
		
		public void setColumnName(String columnName)
		{
			this.columnName = columnName;
		}
		
		public String getPropertyName()
		{
			return propertyName;
		}
		
		public void setPropertyName(String propertyName)
		{
			this.propertyName = propertyName;
		}
		
		public Field getField()
		{
			return field;
		}
		
		public void setField(Field field)
		{
			this.field = field;
		}
		
	}
	
}
