package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.sql.Timestamp;

public class TimeStampTransfer extends AbstractResultsetTransfer<Timestamp>
{
    
    public TimeStampTransfer(Class<?> ckass)
    {
        super(ckass);
    }

    @Override
    protected Timestamp valueOf(ResultSet resultSet, String sql) throws Exception
    {
        return resultSet.getTimestamp(1);
    }
    
}
