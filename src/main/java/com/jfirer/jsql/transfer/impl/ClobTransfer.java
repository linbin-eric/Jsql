package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.Clob;
import java.sql.ResultSet;

public class ClobTransfer implements ResultSetTransfer
{
    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        Clob clob = resultSet.getClob(columnIndex);
        return clob;
    }
}
