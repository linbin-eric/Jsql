package cc.jfire.jsql.test.vo;

import cc.jfire.baseutil.TRACEID;
import cc.jfire.jsql.dialect.Dialect;
import cc.jfire.jsql.executor.impl.NextHolder;
import cc.jfire.jsql.metadata.TableEntityInfo;
import cc.jfire.jsql.transfer.ResultSetTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SqlLog extends NextHolder
{
    private static final Logger logger = LoggerFactory.getLogger(SqlLog.class);

    @Override
    public int update(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        logger.trace("traceId:{} 执行的sql:{},参数：{}", TRACEID.currentTraceId(), sql, params);
        return next.update(sql, params, connection, dialect);
    }

    @Override
    public String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect, TableEntityInfo.ColumnInfo pkInfo) throws SQLException
    {
        logger.trace("traceId:{} 执行的sql:{},参数：{}", TRACEID.currentTraceId(), sql, params);
        return next.insertWithReturnKey(sql, params, connection, dialect, pkInfo);
    }

    @Override
    public List<Object> queryList(String sql, ResultSetTransfer transfer, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        logger.debug("traceId:{} 执行的sql:{}", TRACEID.currentTraceId(), sql);
        return next.queryList(sql, transfer, params, connection, dialect);
    }

    @Override
    public Object queryOne(String sql, ResultSetTransfer transfer, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        logger.debug("traceId:{} 执行的sql:{}", TRACEID.currentTraceId(), sql);
        return next.queryOne(sql, transfer, params, connection, dialect);
    }

    @Override
    public int order()
    {
        return 2000;
    }
}
