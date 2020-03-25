package com.jfirer.jsql.executor;

import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.transfer.resultset.ResultSetTransfer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface SqlInvoker
{
    int update(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException;

    String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException;

    List<Object> queryList(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer) throws SQLException;

    Object queryOne(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer) throws SQLException;

}
