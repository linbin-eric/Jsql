package com.jfireframework.sql.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

public interface SqlExecutor
{
	int update(String sql, List<Object> params, Connection connection, Dialect dialect, SqlInvoker next) throws SQLException;
	
	String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect, SqlInvoker next) throws SQLException;
	
	List<Object> queryList(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer, SqlInvoker next) throws SQLException;
	
	Object queryOne(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer, SqlInvoker next) throws SQLException;
	
	// 拦截器顺序，数字越大，越后执行
	int order();
}
