package com.jfireframework.sql.session.sqloperation;

import java.util.List;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;

public interface SqlOperator
{
    public <T> T query(ResultSetTransfer<T> transfer, String sql, Object... params);
    
    public <T> List<T> queryList(ResultSetTransfer<T> transfer, String sql, Object... params);
    
    public <T> List<T> queryList(ResultSetTransfer<T> transfer, String sql, Page page, Object... params);
    
    public int update(String sql, Object... params);
}
