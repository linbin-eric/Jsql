package com.jfireframework.sql.transfer.resultset.impl;

import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LongTransfer implements ResultSetTransfer
{

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        long l = resultSet.getLong(1);
        if ( resultSet.wasNull() )
        {
            return null;
        }
        return l;
    }

    @Override
    public ResultSetTransfer initialize(Class<?> type)
    {
        return this;
    }

}
