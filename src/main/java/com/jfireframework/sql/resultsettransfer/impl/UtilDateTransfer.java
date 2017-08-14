package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class UtilDateTransfer extends AbstractResultsetTransfer
{
    
    @Override
    protected Date valueOf(ResultSet resultSet, String sql) throws Exception
    {
        Timestamp timestamp = resultSet.getTimestamp(1);
        return new Date(timestamp.getTime());
    }
    
    @Override
    public void initialize(Class<?> type, JdbcTypeDictionary jdbcTypeDictionary)
    {
    }
    
}
