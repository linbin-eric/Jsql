package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class UtilDateTransfer extends ColumnNameHolder
{
    public UtilDateTransfer(String columnName)
    {
        super(columnName);
    }

    public UtilDateTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        Timestamp timestamp = columnName == null ? resultSet.getTimestamp(1) : resultSet.getTimestamp(columnName);
        if (timestamp != null)
        {
            return new Date(timestamp.getTime());
        }
        else
        {
            return null;
        }
    }
}
