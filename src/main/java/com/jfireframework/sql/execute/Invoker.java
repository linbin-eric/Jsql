package com.jfireframework.sql.execute;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

public interface Invoker
{
	Object update(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException;
	
	String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException;
	
	List<Object> queryList(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer) throws SQLException;
	
	Object queryOne(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer) throws SQLException;
	
}
