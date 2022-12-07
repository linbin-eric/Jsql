package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TimeStampTransfer extends ColumnIndexHolder
{
    public TimeStampTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public TimeStampTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        return resultSet.getTimestamp(1);
    }
}
