package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

public class CalendarTransfer extends ColumnNameHolder
{
    public CalendarTransfer(String columnName)
    {
        super(columnName);
    }

    public CalendarTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        Timestamp timestamp = columnName == null ? resultSet.getTimestamp(1) : resultSet.getTimestamp(columnName);
        if (timestamp != null)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp.getTime());
            return calendar;
        }
        else
        {
            return null;
        }
    }
}
