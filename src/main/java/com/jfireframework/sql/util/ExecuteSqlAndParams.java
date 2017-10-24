package com.jfireframework.sql.util;

public class ExecuteSqlAndParams
{
	protected final String		sql;
	protected final Object[]	params;
	
	public ExecuteSqlAndParams(String sql, Object... params)
	{
		this.sql = sql;
		this.params = params;
	}
	
	public String getSql()
	{
		return sql;
	}
	
	public Object[] getParams()
	{
		return params;
	}
}
