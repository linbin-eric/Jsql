package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class FloatTransfer extends AbstractResultsetTransfer
{
    
    @Override
    protected Float valueOf(ResultSet resultSet, String sql) throws Exception
    {
        return Float.valueOf(resultSet.getFloat(1));
    }
    
    @Override
    public void initialize(Class<?> type, JdbcTypeDictionary jdbcTypeDictionary)
    {
    }
    
}
