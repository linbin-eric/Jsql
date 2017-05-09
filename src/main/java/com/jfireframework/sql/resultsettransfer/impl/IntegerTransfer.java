package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegerTransfer extends AbstractResultsetTransfer<Integer>
{
    
    public IntegerTransfer(Class<?> ckass)
    {
        super(ckass);
    }

    @Override
    protected Integer valueOf(ResultSet resultSet, String sql) throws SQLException
    {
        return Integer.valueOf(resultSet.getInt(1));
    }
    
}
