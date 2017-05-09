package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;

public class LongTransfer extends AbstractResultsetTransfer<Long>
{
    public LongTransfer(Class<?> ckass)
    {
        super(ckass);
    }

    @Override
    protected Long valueOf(ResultSet resultSet, String sql) throws Exception
    {
        return Long.valueOf(resultSet.getLong(1));
    }
    
}
