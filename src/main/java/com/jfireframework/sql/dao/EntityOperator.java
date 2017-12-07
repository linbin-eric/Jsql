package com.jfireframework.sql.dao;

import java.sql.Connection;
import java.util.List;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;

public interface EntityOperator<T>
{
	void initialize(TableMetaData<T> metaData, SessionfactoryConfig config, SqlInterceptor[] sqlInterceptors, String tableName, PageParse pageParse, Dialect dialect);
	
	int insert(Connection connection, String strategy, Object... params);
	
	int delete(Connection connection, String strategy, Object... params);
	
	int update(Connection connection, String strategy, Object... params);
	
	T findOne(Connection connection, String strategy, Object... params);
	
	T findOneForUpdate(Connection connection, String strategy, Object... params);
	
	T findOneForShare(Connection connection, String strategy, Object... params);
	
	List<T> findAll(Connection connection, String strategy, Object... params);
	
	List<T> findPage(Connection connection, Page page, String strategy, Object... params);
	
	int count(Connection connection, String strategy, Object... params);
}
