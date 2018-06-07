package com.jfireframework.sql.model;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.Column;
import com.jfireframework.sql.annotation.ColumnNameStrategyDef;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.metadata.ColumnNameStrategy;
import com.jfireframework.sql.metadata.DefaultLowerCaseNameStrategy;

public abstract class Model<T>
{
	protected Class<T>											entityClass;
	protected List<String>										whereProperties;
	protected boolean											frozen				= false;
	protected String											generateSql;
	protected static final Map<Class<?>, Map<String, String>>	cachedColumnName	= new ConcurrentHashMap<Class<?>, Map<String, String>>();
	
	/**
	 * 返回一个属性名和字段名的映射
	 * 
	 * @return
	 */
	protected Map<String, String> getColumnNameMap()
	{
		Map<String, String> map = cachedColumnName.get(entityClass);
		if (map == null)
		{
			map = new HashMap<String, String>();
			ColumnNameStrategy strategy;
			try
			{
				strategy = entityClass.isAnnotationPresent(ColumnNameStrategyDef.class) ? //
				        entityClass.getAnnotation(ColumnNameStrategyDef.class).value().newInstance()//
				        : DefaultLowerCaseNameStrategy.instance;
			}
			catch (Exception e)
			{
				throw new JustThrowException(e);
			}
			for (Field field : ReflectUtil.getAllFields(entityClass))
			{
				map.put(field.getName(), getColumnName(strategy, field));
			}
		}
		return map;
	}
	
	/**
	 * @param strategy
	 * @param field
	 * @return
	 */
	private String getColumnName(ColumnNameStrategy strategy, Field field)
	{
		String columnName;
		if (field.isAnnotationPresent(Column.class) && StringUtil.isNotBlank(field.getAnnotation(Column.class).name()))
		{
			columnName = field.getAnnotation(Column.class).name();
		}
		else
		{
			columnName = strategy.toColumnName(field.getName());
		}
		return columnName;
	}
	
	protected void check()
	{
		if (frozen)
		{
			throw new IllegalStateException("已经是冻结状态，不允许改动");
		}
	}
	
	protected void generateBefore()
	{
		if (frozen != false)
		{
			throw new IllegalStateException("已经生成过，不能该次修改");
		}
		frozen = true;
	}
	
	public abstract T generate();
	
	/**
	 * @param cache
	 * @param columnNameMap
	 */
	protected void setWhereColumns(StringCache cache, Map<String, String> columnNameMap)
	{
		if (whereProperties != null)
		{
			cache.append("where ");
			for (String each : whereProperties)
			{
				String columnName = columnNameMap.get(each);
				cache.append(columnName).append("=? and ");
			}
			cache.deleteEnds(4);
		}
	}
	
	public String getSql()
	{
		return generateSql;
	}
	
	@SuppressWarnings("unchecked")
	public T from(Class<T> entityClass)
	{
		if (entityClass.isAnnotationPresent(TableEntity.class) == false)
		{
			throw new IllegalArgumentException("没有实体类注解");
		}
		check();
		this.entityClass = entityClass;
		return (T) this;
	}
	
	@SuppressWarnings("unchecked")
	public T where(String propertyName)
	{
		check();
		if (whereProperties == null)
		{
			whereProperties = new LinkedList<String>();
		}
		whereProperties.add(propertyName);
		return (T) this;
	}
	
	public Class<T> getEntityClass()
	{
		return entityClass;
	}
}
