package com.jfireframework.sql.page;

import com.jfireframework.sql.util.ExecuteSqlAndParams;

public interface PageParse
{
	/**
	 * 返回2个信息。第一个是查询的，第二个是统计
	 * 
	 * @param page
	 * @param sql
	 * @param params
	 * @return
	 */
	ExecuteSqlAndParams[] parseQuery(Page page, String sql, Object... params);
	
	/**
	 * 返回查询的信息
	 * 
	 * @param page
	 * @param sql
	 * @param params
	 * @return
	 */
	ExecuteSqlAndParams parseQeuryWithoutCount(Page page, String sql, Object... params);
	
}
