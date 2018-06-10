package com.jfireframework.sql.model;

import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.TableDef;

public class DeleteModel extends Model<DeleteModel>
{
	
	@Override
	public DeleteModel generate()
	{
		generateBefore();
		StringCache cache = new StringCache();
		cache.append("delete from ").append(entityClass.getAnnotation(TableDef.class).name()).append(" ");
		setWhereColumns(cache, getColumnNameMap());
		generateSql = cache.toString();
		return this;
	}
	
}
