package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;

public class ShortTransfer extends AbstractResultsetTransfer<Short>
{
    
    public ShortTransfer(Class<?> ckass)
    {
        super(ckass);
    }

    @Override
    protected Short valueOf(ResultSet resultSet, String sql) throws Exception
    {
        return Short.valueOf(resultSet.getShort(1));
    }
    
}
