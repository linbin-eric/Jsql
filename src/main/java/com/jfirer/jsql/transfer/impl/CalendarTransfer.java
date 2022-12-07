package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

public class CalendarTransfer extends ColumnIndexHolder
{
    public CalendarTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public CalendarTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        Timestamp timestamp = resultSet.getTimestamp(columnIndex);
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
