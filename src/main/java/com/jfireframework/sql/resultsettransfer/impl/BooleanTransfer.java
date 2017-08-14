package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class BooleanTransfer extends AbstractResultsetTransfer
{
    
    @Override
    protected Boolean valueOf(ResultSet resultSet, String sql) throws Exception
    {
        return Boolean.valueOf(resultSet.getBoolean(1));
    }
    
    @Override
    public void initialize(Class<?> type, JdbcTypeDictionary jdbcTypeDictionary)
    {
    }
    
}
