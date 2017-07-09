package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;

public class DoubleTransfer extends AbstractResultsetTransfer
{
    
    @Override
    protected Double valueOf(ResultSet resultSet, String sql) throws Exception
    {
        return Double.valueOf(resultSet.getDouble(1));
    }
    
    @Override
    public void initialize(Class<?> type)
    {
    }
    
}
