package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;

public class DoubleTransfer extends AbstractResultsetTransfer<Double>
{
    
    public DoubleTransfer(Class<?> ckass)
    {
        super(ckass);
    }

    @Override
    protected Double valueOf(ResultSet resultSet, String sql) throws Exception
    {
        return Double.valueOf(resultSet.getDouble(1));
    }
    
}
