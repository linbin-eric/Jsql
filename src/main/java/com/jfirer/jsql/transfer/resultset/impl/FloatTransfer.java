package com.jfirer.jsql.transfer.resultset.impl;

import com.jfirer.jsql.transfer.resultset.ResultSetTransfer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FloatTransfer implements ResultSetTransfer
{

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        float f = resultSet.getFloat(1);
        if ( resultSet.wasNull() )
        {
            return null;
        }
        return f;
    }

    @Override
    public ResultSetTransfer initialize(Class<?> type)
    {
        return this;
    }

}
