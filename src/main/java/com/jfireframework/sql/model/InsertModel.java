package com.jfireframework.sql.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.TableDef;
import com.jfireframework.sql.metadata.TableEntityInfo;
import com.jfireframework.sql.metadata.TableEntityInfo.ColumnInfo;

public class InsertModel extends Model
{
	private List<InsertEntry> insertEntries;
	
	class InsertEntry
	{
		String	propertyName;
		Object	value;
		
		public InsertEntry(String propertyName, Object value)
		{
			this.propertyName = propertyName;
			this.value = value;
		}
		
	}
	
	public Model insert(String property, Object value)
	{
		if (insertEntries == null)
		{
			insertEntries = new LinkedList<InsertEntry>();
		}
		insertEntries.add(new InsertEntry(property, value));
		return this;
	}
	
	@Override
    public String _getSql()
	{
		StringCache cache = new StringCache();
		cache.append("insert into ").append(entityClass.getAnnotation(TableDef.class).name()).append(" (");
		if (insertEntries == null)
		{
			throw new NullPointerException("需要插入的属性为空");
		}
		Map<String, ColumnInfo> columnInfoMap = TableEntityInfo.parse(entityClass).getPropertyNameKeyMap();
		for (InsertEntry each : insertEntries)
		{
			cache.append(columnInfoMap.get(each.propertyName).getColumnName()).appendComma();
		}
		cache.deleteLast().append(") values(");
		int size = insertEntries.size();
		for (int i = 0; i < size; i++)
		{
			cache.append("?,");
		}
		cache.deleteLast().append(')');
		return cache.toString();
	}
	
	@Override
	public List<Object> getParams()
	{
		List<Object> params = new LinkedList<Object>();
		if (insertEntries != null)
		{
			for (InsertEntry insertEntry : insertEntries)
			{
				params.add(insertEntry.value);
			}
		}
		return params;
	}
	
}
