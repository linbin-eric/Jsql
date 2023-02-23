package com.jfirer.jsql.executor.impl;

import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.transfer.ResultSetTransfer;
import com.jfirer.jsql.transfer.impl.IntegerTransfer;

import java.lang.reflect.AnnotatedElement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class StandardPageExecutor extends NextHolder
{
    private final ResultSetTransfer countResultTransfer = new IntegerTransfer();

    @Override
    public int update(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        return next.update(sql, params, connection, dialect);
    }

    @Override
    public String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        return next.insertWithReturnKey(sql, params, connection, dialect);
    }

    @Override
    public List<Object> queryList(String sql, AnnotatedElement element, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        Object param;
        if (params.isEmpty() || !((param = params.get(params.size() - 1)) instanceof Page))
        {
            return next.queryList(sql, element, params, connection, dialect);
        }
        params.remove(params.size() - 1);
        Page page = (Page) param;
        if (page.isFetchSum())
        {
            String countSql = "select count(1) from (" + sql + ") as a";
            int    total    = (Integer) next.queryOne(countSql, Integer.class, params, connection, dialect);
            page.setTotal(total);
        }
        sql = sql + " limit ?,?";
        params.add(page.getOffset());
        params.add(page.getSize());
        return next.queryList(sql, element, params, connection, dialect);
    }

    @Override
    public Object queryOne(String sql, AnnotatedElement element, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        return next.queryOne(sql, element, params, connection, dialect);
    }

    @Override
    public int order()
    {
        return 1000;
    }
}
