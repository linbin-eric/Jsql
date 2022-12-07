package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TimeStampTransfer extends ColumnNameHolder
{
    public TimeStampTransfer(String columnName)
    {
        super(columnName);
    }

    public TimeStampTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        return columnName == null ? resultSet.getTimestamp(1) : resultSet.getTimestamp(columnName);
    }
}
