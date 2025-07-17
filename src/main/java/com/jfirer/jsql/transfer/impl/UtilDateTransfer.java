package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

public class UtilDateTransfer implements ResultSetTransfer
{
    public static final UtilDateTransfer INSTANCE = new UtilDateTransfer();

    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
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
