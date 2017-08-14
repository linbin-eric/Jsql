package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.Date;
import java.sql.ResultSet;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class SqlDateTransfer extends AbstractResultsetTransfer
{
    
    @Override
    protected Date valueOf(ResultSet resultSet, String sql) throws Exception
    {
        return resultSet.getDate(1);
    }
    
    @Override
    public void initialize(Class<?> type, JdbcTypeDictionary jdbcTypeDictionary)
    {
    }
    
}
