package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringTransfer extends AbstractResultsetTransfer<String>
{
    
    public StringTransfer(Class<?> ckass)
    {
        super(ckass);
    }

    @Override
    protected String valueOf(ResultSet resultSet, String sql) throws SQLException
    {
        return resultSet.getString(1);
    }
    
}
