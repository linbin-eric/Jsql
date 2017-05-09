package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.sql.Time;

public class TimeTransfer extends AbstractResultsetTransfer<Time>
{
    public TimeTransfer(Class<?> ckass)
    {
        super(ckass);
    }

    @Override
    protected Time valueOf(ResultSet resultSet, String sql) throws Exception
    {
        return resultSet.getTime(1);
    }
    
}
