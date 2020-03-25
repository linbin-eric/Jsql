package com.jfirer.jsql.transfer.resultset.impl;

import com.jfirer.jsql.transfer.resultset.ResultSetTransfer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShortTransfer implements ResultSetTransfer
{

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        short s = resultSet.getShort(1);
        if ( resultSet.wasNull() )
        {
            return null;
        }
        return s;
    }

    @Override
    public ResultSetTransfer initialize(Class<?> type)
    {
        return this;
    }

}
