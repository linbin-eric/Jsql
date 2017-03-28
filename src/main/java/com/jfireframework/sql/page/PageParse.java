package com.jfireframework.sql.page;

import java.sql.Connection;
import java.sql.SQLException;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;
import com.jfireframework.sql.resultsettransfer.field.MapField;

public interface PageParse
{
    public void doQuery(Object[] params, Connection connection, String sql, ResultSetTransfer<?> transfer, Page page) throws SQLException;
    
    public void doQuery(Object entity, MapField[] fields, Connection connection, String sql, ResultSetTransfer<?> transfer, Page page) throws SQLException;
}
