package com.jfireframework.sql.dao;

import java.sql.Connection;
import java.util.List;
import com.jfireframework.sql.page.Page;

public interface StrategyOperation<T>
{
	int insert(Connection connection, String strategy, Object... params);
	
	int delete(Connection connection, String strategy, Object... params);
	
	int update(Connection connection, String strategy, Object... params);
	
	T findOne(Connection connection, String strategy, Object... params);
	
	List<T> findAll(Connection connection, String strategy, Object... params);
	
	List<T> findPage(Connection connection, Page page, String strategy, Object... params);
	
	int count(Connection connection, String strategy, Object... params);
}
