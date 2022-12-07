package com.jfirer.jsql.test.vo;

import com.jfirer.baseutil.TRACEID;
import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.executor.SqlExecutor;
import com.jfirer.jsql.executor.impl.NextHolder;
import com.jfirer.jsql.transfer.resultset.ResultSetTransfer;
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
        logger.debug("traceId:{} 执行的sql:{},参数：{}", TRACEID.currentTraceId(), sql, params);
        return next.update(sql, params, connection, dialect);
    }

    @Override
    public String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        logger.debug("traceId:{} 执行的sql:{}", TRACEID.currentTraceId(), sql);
        return next.insertWithReturnKey(sql, params, connection, dialect);
    }

    @Override
    public List<Object> queryList(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer) throws SQLException
    {
        logger.debug("traceId:{} 执行的sql:{}", TRACEID.currentTraceId(), sql);
        return next.queryList(sql, params, connection, dialect, resultSetTransfer);
    }

    @Override
    public Object queryOne(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer) throws SQLException
    {
        logger.debug("traceId:{} 执行的sql:{}", TRACEID.currentTraceId(), sql);
        return next.queryOne(sql, params, connection, dialect, resultSetTransfer);
    }

    @Override
    public int order()
    {
        return 2000;
    }
}
