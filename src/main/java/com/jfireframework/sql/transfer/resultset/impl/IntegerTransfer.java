package com.jfireframework.sql.transfer.resultset.impl;

import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegerTransfer implements ResultSetTransfer
{

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        int i = resultSet.getInt(1);
        if ( resultSet.wasNull() )
        {
            return null;
        }
        return i;
    }

    @Override
    public ResultSetTransfer initialize(Class<?> type)
    {
        return this;
    }

}
