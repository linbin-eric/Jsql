package com.jfireframework.sql.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.TableDef;

public class UpdateModel extends Model<UpdateModel>
{
	private List<String> setProperties;
	
	public UpdateModel set(String property)
	{
		check();
		if (setProperties == null)
		{
			setProperties = new LinkedList<String>();
		}
		setProperties.add(property);
		return this;
	}
	
	@Override
	public UpdateModel generate()
	{
		generateBefore();
		StringCache cache = new StringCache();
		cache.append("update ").append(entityClass.getAnnotation(TableDef.class).name()).append(" ");
		if (setProperties == null)
		{
			throw new IllegalArgumentException("没有设置需要更新的字段");
		}
		cache.append("set ");
		Map<String, String> columnNameMap = getColumnNameMap();
		for (String each : setProperties)
		{
			cache.append(columnNameMap.get(each)).append("=?,");
		}
		cache.deleteLast().append(' ');
		setWhereColumns(cache, columnNameMap);
		generateSql = cache.toString();
		return this;
	}
	
}
