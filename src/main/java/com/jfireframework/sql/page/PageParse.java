package com.jfireframework.sql.page;

import java.sql.Connection;
import java.sql.SQLException;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;

public interface PageParse
{
    void doQuery(Object[] params, Connection connection, String sql, ResultSetTransfer<?> transfer, Page page) throws SQLException;
}
