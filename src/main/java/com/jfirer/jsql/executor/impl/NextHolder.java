package com.jfirer.jsql.executor.impl;

import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.executor.SqlExecutor;

import java.lang.reflect.AnnotatedElement;
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
    public void batchInsert(String sql, List<?> params, Connection connection, Dialect dialect)
    {
        next.batchInsert(sql, params, connection, dialect);
    }

    @Override
    public int update(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        return next.update(sql, params, connection, dialect);
    }

    @Override
    public Object queryOne(String sql, AnnotatedElement element, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        return next.queryOne(sql, element, params, connection, dialect);
    }
}
