package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class UtilDateTransfer extends ColumnIndexHolder
{
    public UtilDateTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public UtilDateTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        Timestamp timestamp = resultSet.getTimestamp(columnIndex);
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
