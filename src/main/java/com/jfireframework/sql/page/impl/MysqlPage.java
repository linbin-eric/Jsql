package com.jfireframework.sql.page.impl;

import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.page.PageSqlCache;
import com.jfireframework.sql.util.ExecuteSqlAndParams;

/**
 * 基于sql92语法,使用limit的方式进行分页
 * 
 * @author linbin
 *
 */
public class MysqlPage implements PageParse
{
	private String parseQuerySql(String originSql)
	{
		String querySql = PageSqlCache.getQuerySql(originSql);
		if (querySql == null)
		{
			querySql = originSql + " limit ?,?";
			PageSqlCache.putQuerySql(originSql, querySql);
		}
		return querySql;
	}
	
	private String parseCountSql(String originSql)
	{
		String countSql = PageSqlCache.getCountSql(originSql);
		if (countSql == null)
		{
			countSql = "select count(*) from ( " + originSql + " )";
			PageSqlCache.putCountSql(originSql, countSql);
		}
		return countSql;
	}
	
	@Override
	public ExecuteSqlAndParams[] parseQuery(Page page, String sql, Object... params)
	{
		String querySql = parseQuerySql(sql);
		String countSql = parseCountSql(sql);
		Object[] newParams = new Object[params.length + 2];
		System.arraycopy(params, 0, newParams, 0, params.length);
		newParams[params.length] = page.getOffset();
		newParams[params.length + 1] = page.getSize();
		ExecuteSqlAndParams query = new ExecuteSqlAndParams(querySql, newParams);
		ExecuteSqlAndParams count = new ExecuteSqlAndParams(countSql, params);
		return new ExecuteSqlAndParams[] { query, count };
	}
	
	@Override
	public ExecuteSqlAndParams parseQeuryWithoutCount(Page page, String sql, Object... params)
	{
		String querySql = parseQuerySql(sql);
		Object[] newParams = new Object[params.length + 2];
		System.arraycopy(params, 0, newParams, 0, params.length);
		newParams[params.length] = page.getOffset();
		newParams[params.length + 1] = page.getSize();
		ExecuteSqlAndParams query = new ExecuteSqlAndParams(querySql, newParams);
		return query;
	}
	
}
