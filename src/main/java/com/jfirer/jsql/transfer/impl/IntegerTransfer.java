package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class IntegerTransfer implements ResultSetTransfer
{
    public static  final IntegerTransfer INSTANCE = new IntegerTransfer();
    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        int i = resultSet.getInt(columnIndex);
        if (resultSet.wasNull())
        {
            return null;
        }
        return i;
    }
}
