package com.jfirer.jsql.executor.impl;

import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.executor.SqlExecutor;
import com.jfirer.jsql.transfer.ResultSetTransfer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public abstract class NextHolder implements SqlExecutor
{
    protected SqlExecutor next;

    @Override
    public void setNext(SqlExecutor next)
    {
        this.next = next;
    }

    @Override
    public int update(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        return next.update(sql, params, connection, dialect);
    }

    @Override
    public Object queryOne(String sql, ResultSetTransfer transfer, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        return next.queryOne(sql, transfer, params, connection, dialect);
    }
}
