package com.jfireframework.sql.model;

import java.util.LinkedList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.TableDef;

public class DeleteModel extends Model
{
	
	@Override
    public String _getSql()
	{
		StringCache cache = new StringCache();
		cache.append("delete from ").append(entityClass.getAnnotation(TableDef.class).name()).append(" ");
		setWhereColumns(cache);
		return cache.toString();
	}
	
	@Override
	public List<Object> getParams()
	{
		List<Object> params = new LinkedList<Object>();
		if (whereEntries != null)
		{
			for (WhereEntry entry : whereEntries)
			{
				params.add(entry.value);
			}
		}
		return params;
	}
	
}
