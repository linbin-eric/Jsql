package com.jfireframework.sql.dao;

import java.sql.Connection;
import java.util.List;
import com.jfireframework.sql.page.Page;

public interface StrategyOperation
{
	public int delete(Connection connection, String strategy, Object... params);
	
	public int update(Connection connection, String strategy, Object... params);
	
	public <T> T findOne(Connection connection, String strategy, Object... params);
	
	public <T> List<T> findAll(Connection connection, String strategy, Object... params);
	
	public <T> List<T> findPage(Connection connection, Page page, String strategy, Object... params);
	
	public int count(Connection connection, String strategy, Object... params);
}
