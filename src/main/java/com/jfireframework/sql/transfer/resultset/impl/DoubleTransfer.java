package com.jfireframework.sql.transfer.resultset.impl;

import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DoubleTransfer implements ResultSetTransfer
{

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        double d = resultSet.getDouble(1);
        if ( resultSet.wasNull() )
        {
            return null;
        }
        return d;
    }

    @Override
    public ResultSetTransfer initialize(Class<?> type)
    {
        return this;
    }

}
