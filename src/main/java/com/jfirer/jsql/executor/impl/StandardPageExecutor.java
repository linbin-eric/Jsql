package com.jfirer.jsql.executor.impl;

import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.executor.SqlExecutor;
import com.jfirer.jsql.executor.SqlInvoker;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.transfer.resultset.ResultSetTransfer;
import com.jfirer.jsql.transfer.resultset.impl.IntegerTransfer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class StandardPageExecutor implements SqlExecutor
{
    private final ResultSetTransfer countResultTransfer = new IntegerTransfer();

    @Override
    public int update(String sql, List<Object> params, Connection connection, Dialect dialect, SqlInvoker next) throws SQLException
    {
        return next.update(sql, params, connection, dialect);
    }

    @Override
    public String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect, SqlInvoker next) throws SQLException
    {
        return next.insertWithReturnKey(sql, params, connection, dialect);
    }

    @Override
    public List<Object> queryList(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer, SqlInvoker next) throws SQLException
    {
        Object param;
        if ( params.isEmpty() || (param = params.get(params.size() - 1)) instanceof Page == false )
        {
            return next.queryList(sql, params, connection, dialect, resultSetTransfer);
        }
        params.remove(params.size() - 1);
        Page page = (Page) param;
        if ( page.isFetchSum() )
        {
            String countSql = "select count(*) from (" + sql + ")";
            int total = (Integer) next.queryOne(countSql, params, connection, dialect, countResultTransfer);
            page.setTotal(total);
        }
        sql = sql + " limit ?,?";
        params.add(page.getOffset());
        params.add(page.getSize());
        return next.queryList(sql, params, connection, dialect, resultSetTransfer);
    }

    @Override
    public Object queryOne(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer, SqlInvoker next) throws SQLException
    {
        return next.queryOne(sql, params, connection, dialect, resultSetTransfer);
    }

    @Override
    public int order()
    {
        return 1000;
    }

}
