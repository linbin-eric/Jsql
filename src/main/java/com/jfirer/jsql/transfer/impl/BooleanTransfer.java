package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class BooleanTransfer implements ResultSetTransfer
{
    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        boolean b = resultSet.getBoolean(columnIndex);
        if (resultSet.wasNull())
        {
            return null;
        }
        return b;
    }
}
