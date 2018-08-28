package com.jfireframework.sql.executor.impl;

import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.executor.SqlExecutor;
import com.jfireframework.sql.executor.SqlInvoker;
import com.jfireframework.sql.metadata.Page;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;
import com.jfireframework.sql.transfer.resultset.impl.IntegerTransfer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class OraclePageExecutor implements SqlExecutor
{
    private ResultSetTransfer countResultTransfer = new IntegerTransfer();

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
        Object param = params.get(params.size() - 1);
        if ( param instanceof Page == false )
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
        sql = "select * from ( select a.*,rownum rn from(" + sql + ") a where rownum<=?) where rn>=?";
        params.add(page.getOffset() + page.getSize());
        params.add(page.getOffset() + 1);
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
