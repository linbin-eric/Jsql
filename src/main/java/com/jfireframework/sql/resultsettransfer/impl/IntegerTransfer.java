package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class IntegerTransfer extends AbstractResultsetTransfer
{
    
    @Override
    protected Integer valueOf(ResultSet resultSet, String sql) throws SQLException
    {
        return Integer.valueOf(resultSet.getInt(1));
    }
    
    @Override
    public void initialize(Class<?> type, JdbcTypeDictionary jdbcTypeDictionary)
    {
    }
    
}
