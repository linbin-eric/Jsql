package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class LongTransfer implements ResultSetTransfer
{
    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        long l = resultSet.getLong(columnIndex);
        if (resultSet.wasNull())
        {
            return null;
        }
        return l;
    }
}
