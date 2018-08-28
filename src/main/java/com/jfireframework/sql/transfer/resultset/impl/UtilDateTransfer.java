package com.jfireframework.sql.transfer.resultset.impl;

import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class UtilDateTransfer implements ResultSetTransfer
{

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        Timestamp timestamp = resultSet.getTimestamp(1);
        if ( timestamp != null )
        {
            return new Date(timestamp.getTime());
        }
        else
        {
            return null;
        }
    }

    @Override
    public ResultSetTransfer initialize(Class<?> type)
    {
        return this;
    }

}
