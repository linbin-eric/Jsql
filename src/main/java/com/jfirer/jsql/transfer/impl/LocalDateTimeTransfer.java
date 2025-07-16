package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.sql.Timestamp;

public class LocalDateTimeTransfer implements ResultSetTransfer
{
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
