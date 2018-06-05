package com.jfireframework.sql.execute;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

public interface SqlExecutor
{
	Object update(String sql, List<Object> params, Connection connection, Dialect dialect, Invoker next) throws SQLException;
	
	String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect, Invoker next) throws SQLException;
	
	List<Object> queryList(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer, Invoker next) throws SQLException;
	
	Object queryOne(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer, Invoker next) throws SQLException;
	
	int count(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer, Invoker next) throws SQLException;
	
	// 拦截器顺序，数字越大，越后执行
	int order();
}
