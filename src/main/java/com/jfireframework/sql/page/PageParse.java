package com.jfireframework.sql.page;

import java.sql.Connection;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;

public interface PageParse
{
    void doQuery(Object[] params, Connection connection, String sql, ResultSetTransfer<?> transfer, Page page) throws Exception;
    
    void queryWithoutCount(Object[] params, Connection connection, String sql, ResultSetTransfer<?> transfer, Page page) throws Exception;
}
