package com.jfireframework.sql.resultsettransfer;

import java.sql.ResultSet;
import java.util.List;
import com.jfireframework.sql.SessionfactoryConfig;

public interface ResultSetTransfer
{
    void initialize(Class<?> type, SessionfactoryConfig config);
    
    Object transfer(ResultSet resultSet, String sql) throws Exception;
    
    List<Object> transferList(ResultSet resultSet, String sql) throws Exception;
}
