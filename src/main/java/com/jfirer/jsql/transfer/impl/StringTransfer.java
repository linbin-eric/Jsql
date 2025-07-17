package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class StringTransfer implements ResultSetTransfer
{
    public static final StringTransfer INSTANCE = new StringTransfer();

    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        return resultSet.getString(columnIndex);
    }
}
