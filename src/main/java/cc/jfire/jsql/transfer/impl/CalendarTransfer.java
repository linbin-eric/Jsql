package cc.jfire.jsql.transfer.impl;

import cc.jfire.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;

public class CalendarTransfer implements ResultSetTransfer
{
    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
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
