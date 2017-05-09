package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;

public class FloatTransfer extends AbstractResultsetTransfer<Float>
{
    
    public FloatTransfer(Class<?> ckass)
    {
        super(ckass);
    }

    @Override
    protected Float valueOf(ResultSet resultSet, String sql) throws Exception
    {
        return Float.valueOf(resultSet.getFloat(1));
    }
    
}
