package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.Date;
import java.sql.ResultSet;

public class SqlDateTransfer extends AbstractResultsetTransfer<java.sql.Date>
{
    
    public SqlDateTransfer(Class<?> ckass)
    {
        super(ckass);
    }

    @Override
    protected Date valueOf(ResultSet resultSet, String sql) throws Exception
    {
        return resultSet.getDate(1);
    }
    
}
