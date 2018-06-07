package com.jfireframework.sql.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.transfer.resultset.impl.BeanTransfer;

public class QueryModel extends Model<QueryModel>
{
	private List<String>	selectProperties;
	private List<String>	orderProperties;
	private BeanTransfer	beanTransfer;
	
	public QueryModel select(String propertyName)
	{
		check();
		if (selectProperties == null)
		{
			selectProperties = new LinkedList<String>();
		}
		selectProperties.add(propertyName);
		return this;
	}
	
	public QueryModel orderBy(String propertyName)
	{
		check();
		if (orderProperties == null)
		{
			orderProperties = new LinkedList<String>();
		}
		orderProperties.add(propertyName);
		return this;
	}
	
	public QueryModel generate()
	{
		generateBefore();
		beanTransfer = (BeanTransfer) new BeanTransfer().initialize(entityClass);
		try
		{
			Map<String, String> columnNameMap = getColumnNameMap();
			StringCache cache = new StringCache();
			cache.append("select ");
			if (selectProperties == null)
			{
				cache.append("* ");
			}
			else
			{
				for (String each : selectProperties)
				{
					String columnName = columnNameMap.get(each);
					cache.append(columnName).appendComma();
				}
				cache.deleteLast().append(' ');
			}
			cache.append("from ");
			String tableName = entityClass.getAnnotation(TableEntity.class).name();
			cache.append(tableName);
			setWhereColumns(cache, columnNameMap);
			if (orderProperties != null)
			{
				cache.append("order by ");
				for (String each : orderProperties)
				{
					String columnName = columnNameMap.get(each);
					cache.append(columnName).appendComma();
				}
				cache.deleteLast();
			}
			generateSql = cache.toString();
			return this;
		}
		catch (Exception e)
		{
			throw new JustThrowException(e);
		}
	}
	
	public String getSql()
	{
		return generateSql;
	}
	
	public BeanTransfer getBeanTransfer()
	{
		return beanTransfer;
	}
}
