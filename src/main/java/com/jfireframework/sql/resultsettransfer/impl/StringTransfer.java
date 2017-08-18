package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringTransfer extends AbstractResultsetTransfer
{
    
    @Override
    protected String valueOf(ResultSet resultSet, String sql) throws SQLException
    {
        return resultSet.getString(1);
    }
    
    @Override
    public void initialize(Class<?> type)
    {
    }
    
}
