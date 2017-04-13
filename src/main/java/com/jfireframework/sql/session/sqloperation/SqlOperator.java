package com.jfireframework.sql.session.sqloperation;

import java.util.List;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;
import com.jfireframework.sql.util.IdType;

public interface SqlOperator
{
    public <T> T query(ResultSetTransfer<T> transfer, String sql, Object... params);
    
    public <T> List<T> queryList(ResultSetTransfer<T> transfer, String sql, Object... params);
    
    public <T> List<T> queryList(ResultSetTransfer<T> transfer, String sql, Page page, Object... params);
    
    public int update(String sql, Object... params);
    
    void insert(String sql, Object... params);
    
    void batchInsert(String sql, Object... paramArrays);
    
    Object insertWithReturnPKValue(IdType idType, String[] pkName, String sql, Object... params);
}
