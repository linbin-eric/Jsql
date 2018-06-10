package com.jfireframework.sql.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.TableDef;

public class InsertModel extends Model<InsertModel>
{
	private List<String> insertProperties;
	
	public InsertModel insert(String property)
	{
		if (insertProperties == null)
		{
			insertProperties = new LinkedList<String>();
		}
		insertProperties.add(property);
		return this;
	}
	
	@Override
	public InsertModel generate()
	{
		generateBefore();
		StringCache cache = new StringCache();
		cache.append("insert into ").append(entityClass.getAnnotation(TableDef.class).name()).append(" (");
		if (insertProperties == null)
		{
			throw new NullPointerException("需要插入的属性为空");
		}
		Map<String, String> columnNameMap = getColumnNameMap();
		for (String each : insertProperties)
		{
			cache.append(columnNameMap.get(each)).appendComma();
		}
		cache.deleteLast().append(") values(");
		int size = insertProperties.size();
		for (int i = 0; i < size; i++)
		{
			cache.append("?,");
		}
		cache.deleteLast().append(')');
		generateSql = cache.toString();
		return this;
	}
	
}
