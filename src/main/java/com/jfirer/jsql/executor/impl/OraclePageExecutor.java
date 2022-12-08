package com.jfirer.jsql.executor.impl;

import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.transfer.ResultSetTransfer;
import com.jfirer.jsql.transfer.impl.IntegerTransfer;

import java.lang.reflect.AnnotatedElement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class OraclePageExecutor extends NextHolder
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
        Object param = params.get(params.size() - 1);
        if (param instanceof Page == false)
        {
            return next.queryList(sql, element, params, connection, dialect);
        }
        params.remove(params.size() - 1);
        Page page = (Page) param;
        if (page.isFetchSum())
        {
            String countSql = "select count(*) from (" + sql + ")";
            int    total    = (Integer) next.queryOne(countSql, Integer.class, params, connection, dialect);
            page.setTotal(total);
        }
        sql = "select * from ( select a.*,rownum rn from(" + sql + ") a where rownum<=?) where rn>=?";
        params.add(page.getOffset() + page.getSize());
        params.add(page.getOffset() + 1);
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
