package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;

public class ShortTransfer extends AbstractResultsetTransfer
{
    
    @Override
    protected Short valueOf(ResultSet resultSet, String sql) throws Exception
    {
        return Short.valueOf(resultSet.getShort(1));
    }
    
    @Override
    public void initialize(Class<?> type)
    {
    }
    
}
