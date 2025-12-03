package cc.jfire.jsql.transfer.impl;

import cc.jfire.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.sql.Timestamp;

public class LocalDateTimeTransfer implements ResultSetTransfer
{
    public static final LocalDateTimeTransfer INSTANCE = new LocalDateTimeTransfer();

    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        Timestamp timestamp = resultSet.getTimestamp(columnIndex);
        if (timestamp == null)
        {
            return timestamp.toLocalDateTime();
        }
        else
        {
            return null;
        }
    }
}
