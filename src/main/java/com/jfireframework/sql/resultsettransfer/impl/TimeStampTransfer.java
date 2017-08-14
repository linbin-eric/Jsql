package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.sql.Timestamp;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class TimeStampTransfer extends AbstractResultsetTransfer
{
    
    @Override
    protected Timestamp valueOf(ResultSet resultSet, String sql) throws Exception
    {
        return resultSet.getTimestamp(1);
    }
    
    @Override
    public void initialize(Class<?> type, JdbcTypeDictionary jdbcTypeDictionary)
    {
    }
    
}
