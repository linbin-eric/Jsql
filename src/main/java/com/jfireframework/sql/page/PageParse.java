package com.jfireframework.sql.page;

import java.sql.Connection;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;

public interface PageParse
{
    void doQuery(Object[] params, Connection connection, String sql, ResultSetTransfer transfer, Page page, SqlInterceptor[] interceptors) throws Exception;
    
    void queryWithoutCount(Object[] params, Connection connection, String sql, ResultSetTransfer transfer, Page page, SqlInterceptor[] interceptors) throws Exception;
}
