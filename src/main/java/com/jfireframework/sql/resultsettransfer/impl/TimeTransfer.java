package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.sql.Time;

public class TimeTransfer extends AbstractResultsetTransfer
{
    
    @Override
    protected Time valueOf(ResultSet resultSet, String sql) throws Exception
    {
        return resultSet.getTime(1);
    }
    
    @Override
    public void initialize(Class<?> type)
    {
    }
    
}
