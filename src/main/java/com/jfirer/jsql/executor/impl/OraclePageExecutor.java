package com.jfirer.jsql.executor.impl;

import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.metadata.TableEntityInfo;
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
    public String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect, TableEntityInfo.ColumnInfo pkInfo) throws SQLException
    {
        return next.insertWithReturnKey(sql, params, connection, dialect, pkInfo);
    }

    @Override
    public List<Object> queryList(String sql, ResultSetTransfer transfer, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        Object param = params.get(params.size() - 1);
        if (!(param instanceof Page))
        {
            return next.queryList(sql, transfer, params, connection, dialect);
        }
        params.remove(params.size() - 1);
        Page page = (Page) param;
        if (page.isFetchSum())
        {
            String countSql = "select count(*) from (" + sql + ")";
            int    total    = (Integer) next.queryOne(countSql, countResultTransfer, params, connection, dialect);
            page.setTotal(total);
        }
        sql = "select * from ( select a.*,rownum rn from(" + sql + ") a where rownum<=?) where rn>=?";
        params.add(page.getOffset() + page.getSize());
        params.add(page.getOffset() + 1);
        page.setResult(next.queryList(sql, transfer, params, connection, dialect));
        return page.getResult();
    }

    @Override
    public int order()
    {
        return 1000;
    }
}
