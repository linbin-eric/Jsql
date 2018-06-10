package com.jfireframework.sql.model;

import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.TableDef;

public class CountModel extends Model<CountModel>
{
	
	@Override
	public CountModel generate()
	{
		generateBefore();
		StringCache cache = new StringCache();
		cache.append("select count(*) from ").append(entityClass.getAnnotation(TableDef.class).name()).append(' ');
		setWhereColumns(cache, getColumnNameMap());
		generateSql = cache.toString();
		return this;
	}
	
}
