package com.jfireframework.sql.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.TableDef;
import com.jfireframework.sql.metadata.Page;
import com.jfireframework.sql.metadata.TableEntityInfo;
import com.jfireframework.sql.metadata.TableEntityInfo.ColumnInfo;
import com.jfireframework.sql.transfer.resultset.impl.BeanTransfer;

public abstract class Model
{
	protected Class<?>			entityClass;
	protected List<WhereEntry>	whereEntries;
	
	protected Model()
	{
		// TODO Auto-generated constructor stub
	}
	
	class WhereEntry
	{
		String	propertyName;
		Object	value;
		
		public WhereEntry(String propertyName, Object value)
		{
			this.propertyName = propertyName;
			this.value = value;
		}
		
	}
	
	/**
	 * @param cache
	 * @param columnNameMap
	 */
	protected void setWhereColumns(StringCache cache)
	{
		if (whereEntries != null)
		{
			cache.append(" where ");
			Map<String, ColumnInfo> columnInfoMap = TableEntityInfo.parse(entityClass).getPropertyNameKeyMap();
			for (WhereEntry each : whereEntries)
			{
				String columnName = columnInfoMap.get(each.propertyName).getColumnName();
				cache.append(columnName).append("=? and ");
			}
			cache.deleteEnds(4);
		}
	}
	
	public Model insert(String property, Object value)
	{
		throw new UnsupportedOperationException();
	}
	
	public Model select(String propertyName)
	{
		throw new UnsupportedOperationException();
	}
	
	public BeanTransfer getBeanTransfer()
	{
		throw new UnsupportedOperationException();
	}
	
	public Model set(String property, Object value)
	{
		throw new UnsupportedOperationException();
	}
	
	public Model orderBy(String propertyName, boolean desc)
	{
		throw new UnsupportedOperationException();
	}
	
	public Model setPage(Page page)
	{
		throw new UnsupportedOperationException();
	}
	
	protected Model from(Class<?> entityClass)
	{
		if (entityClass.isAnnotationPresent(TableDef.class) == false)
		{
			throw new IllegalArgumentException("没有实体类注解");
		}
		this.entityClass = entityClass;
		return this;
	}
	
	public Model where(String propertyName, Object value)
	{
		if (whereEntries == null)
		{
			whereEntries = new LinkedList<WhereEntry>();
		}
		whereEntries.add(new WhereEntry(propertyName, value));
		return this;
	}
	
	public Class<?> getEntityClass()
	{
		return entityClass;
	}
	
	public abstract String getSql();
	
	public abstract List<Object> getParams();
	
	public static final Model delete(Class<?> ckass)
	{
		return new DeleteModel().from(ckass);
	}
	
	public static final Model query(Class<?> ckass)
	{
		return new QueryModel().from(ckass);
	}
	
	public static final Model update(Class<?> ckass)
	{
		return new UpdateModel().from(ckass);
	}
	
	public static final Model insert(Class<?> ckass)
	{
		return new InsertModel().from(ckass);
	}
	
	public static final Model count(Class<?> ckass)
	{
		return new CountModel().from(ckass);
	}
}
