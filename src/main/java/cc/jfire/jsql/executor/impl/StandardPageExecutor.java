package cc.jfire.jsql.executor.impl;

import cc.jfire.jsql.dialect.Dialect;
import cc.jfire.jsql.metadata.Page;
import cc.jfire.jsql.metadata.TableEntityInfo;
import cc.jfire.jsql.transfer.ResultSetTransfer;
import cc.jfire.jsql.transfer.impl.IntegerTransfer;

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
    public String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect, TableEntityInfo.ColumnInfo pkInfo) throws SQLException
    {
        return next.insertWithReturnKey(sql, params, connection, dialect, pkInfo);
    }

    @Override
    public List<Object> queryList(String sql, ResultSetTransfer transfer, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        Object param;
        if (params.isEmpty() || !((param = params.get(params.size() - 1)) instanceof Page))
        {
            return next.queryList(sql, transfer, params, connection, dialect);
        }
        params.remove(params.size() - 1);
        Page page = (Page) param;
        if (page.isFetchSum())
        {
            String countSql = "select count(1) from (" + sql + ") as a";
            int    total    = (Integer) next.queryOne(countSql, countResultTransfer, params, connection, dialect);
            page.setTotal(total);
        }
        sql = sql + " limit ?,?";
        params.add(page.getOffset());
        params.add(page.getSize());
        page.setResult(next.queryList(sql, transfer, params, connection, dialect));
        return page.getResult();
    }

    @Override
    public Object queryOne(String sql, ResultSetTransfer transfer, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        return next.queryOne(sql, transfer, params, connection, dialect);
    }

    @Override
    public int order()
    {
        return 1000;
    }
}
