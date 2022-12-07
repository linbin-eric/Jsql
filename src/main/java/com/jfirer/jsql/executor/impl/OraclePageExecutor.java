package com.jfirer.jsql.executor.impl;

import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.transfer.ResultSetTransfer;
import com.jfirer.jsql.transfer.impl.IntegerTransfer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class OraclePageExecutor extends NextHolder
{
    private final ResultSetTransfer countResultTransfer = new IntegerTransfer(1);

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
    public List<Object> queryList(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer) throws SQLException
    {
        Object param = params.get(params.size() - 1);
        if (param instanceof Page == false)
        {
            return next.queryList(sql, params, connection, dialect, resultSetTransfer);
        }
        params.remove(params.size() - 1);
        Page page = (Page) param;
        if (page.isFetchSum())
        {
            String countSql = "select count(*) from (" + sql + ")";
            int    total    = (Integer) next.queryOne(countSql, params, connection, dialect, countResultTransfer);
            page.setTotal(total);
        }
        sql = "select * from ( select a.*,rownum rn from(" + sql + ") a where rownum<=?) where rn>=?";
        params.add(page.getOffset() + page.getSize());
        params.add(page.getOffset() + 1);
        return next.queryList(sql, params, connection, dialect, resultSetTransfer);
    }

    @Override
    public Object queryOne(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer) throws SQLException
    {
        return next.queryOne(sql, params, connection, dialect, resultSetTransfer);
    }

    @Override
    public int order()
    {
        return 1000;
    }
}
