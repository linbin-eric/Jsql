package com.jfireframework.sql.model;

import java.util.LinkedList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.TableDef;
import com.jfireframework.sql.metadata.Page;
import com.jfireframework.sql.metadata.TableEntityInfo;
import com.jfireframework.sql.metadata.TableEntityInfo.ColumnInfo;
import com.jfireframework.sql.transfer.resultset.impl.BeanTransfer;

public class QueryModel extends Model
{
	private List<String>		selectProperties;
	private List<OrderByEntry>	orderByProperties;
	private BeanTransfer		beanTransfer;
	private Page				page;
	
	class OrderByEntry
	{
		String	orderPropertyName;
		boolean	desc	= false;
		
		public OrderByEntry(String orderPropertyName, boolean desc)
		{
			this.orderPropertyName = orderPropertyName;
			this.desc = desc;
		}
		
	}
	
	public QueryModel select(String propertyName)
	{
		if (selectProperties == null)
		{
			selectProperties = new LinkedList<String>();
		}
		selectProperties.add(propertyName);
		return this;
	}
	
	public QueryModel orderBy(String orderPropertyName, boolean desc)
	{
		if (orderByProperties == null)
		{
			orderByProperties = new LinkedList<OrderByEntry>();
		}
		orderByProperties.add(new OrderByEntry(orderPropertyName, desc));
		return this;
	}
	
	@Override
	public BeanTransfer getBeanTransfer()
	{
		if (selectProperties == null)
		{
			selectProperties = new LinkedList<String>();
			for (ColumnInfo columnInfo : TableEntityInfo.parse(entityClass).getPropertyNameKeyMap().values())
			{
				selectProperties.add(columnInfo.getPropertyName());
			}
		}
		beanTransfer = new BeanTransfer();
		beanTransfer.initialize(entityClass);
		beanTransfer.preSetColumnTransfer(selectProperties, TableEntityInfo.parse(entityClass));
		return beanTransfer;
	}
	
	@Override
	public String getSql()
	{
		StringCache cache = new StringCache();
		cache.append("select ");
		if (selectProperties == null)
		{
			selectProperties = new LinkedList<String>();
			for (ColumnInfo columnInfo : TableEntityInfo.parse(entityClass).getPropertyNameKeyMap().values())
			{
				selectProperties.add(columnInfo.getPropertyName());
			}
		}
		TableEntityInfo tableEntityInfo = TableEntityInfo.parse(entityClass);
		for (String each : selectProperties)
		{
			String columnName = tableEntityInfo.getPropertyNameKeyMap().get(each).getColumnName();
			cache.append(columnName).appendComma();
		}
		cache.deleteLast().append(' ');
		cache.append("from ");
		String tableName = entityClass.getAnnotation(TableDef.class).name();
		cache.append(tableName);
		setWhereColumns(cache);
		if (orderByProperties != null)
		{
			cache.append(" order by ");
			for (OrderByEntry each : orderByProperties)
			{
				String columnName = tableEntityInfo.getPropertyNameKeyMap().get(each.orderPropertyName).getColumnName();
				cache.append(columnName).append(each.desc ? " desc" : " asc");
				cache.appendComma();
			}
			cache.deleteLast();
		}
		return cache.toString();
	}
	
	@Override
	public List<Object> getParams()
	{
		List<Object> params = new LinkedList<Object>();
		if (whereEntries != null)
		{
			for (WhereEntry whereEntry : whereEntries)
			{
				params.add(whereEntry.value);
			}
		}
		if (page != null)
		{
			params.add(page);
		}
		return params;
	}
	
	@Override
	public Model setPage(Page page)
	{
		this.page = page;
		return this;
	}
}
