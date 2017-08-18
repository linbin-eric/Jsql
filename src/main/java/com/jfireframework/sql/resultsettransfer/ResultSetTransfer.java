package com.jfireframework.sql.resultsettransfer;

import java.sql.ResultSet;
import java.util.List;

public interface ResultSetTransfer
{
    void initialize(Class<?> type);
    
    Object transfer(ResultSet resultSet, String sql) throws Exception;
    
    List<Object> transferList(ResultSet resultSet, String sql) throws Exception;
}
