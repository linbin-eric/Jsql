package com.jfireframework.sql.util;

import com.jfireframework.sql.mapfield.MapField;

public class ExecuteSqlInfo
{
	protected final String		sql;
	protected final MapField[]	params;
	
	public ExecuteSqlInfo(String sql, MapField[] fields)
	{
		this.sql = sql;
		this.params = fields;
	}
	
	public String getSql()
	{
		return sql;
	}
	
	public MapField[] getColumns()
	{
		return params;
	}
	
}
