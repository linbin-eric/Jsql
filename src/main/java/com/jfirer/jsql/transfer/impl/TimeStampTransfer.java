package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class TimeStampTransfer implements ResultSetTransfer
{
    public static final TimeStampTransfer INSTANCE = new TimeStampTransfer();

    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        return resultSet.getTimestamp(columnIndex);
    }
}
