package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class SqlDateTransfer implements ResultSetTransfer
{
    public static final SqlDateTransfer INSTANCE = new SqlDateTransfer();

    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        return resultSet.getDate(columnIndex);
    }
}
