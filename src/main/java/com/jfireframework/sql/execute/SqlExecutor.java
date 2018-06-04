package com.jfireframework.sql.execute;

import java.sql.Connection;
import java.util.List;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

public interface SqlExecutor
{
	Object execute(String sql, List<Object> params, Connection connection, Dialect dialect);
	
	Object queryList(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer<?> resultSetTransfer);
	
	Object queryOne(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer<?> resultSetTransfer);
}
