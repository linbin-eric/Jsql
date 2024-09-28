package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class ShortTransfer implements ResultSetTransfer
{
    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        short s = resultSet.getShort(columnIndex);
        if (resultSet.wasNull())
        {
            return null;
        }
        return s;
    }
}
