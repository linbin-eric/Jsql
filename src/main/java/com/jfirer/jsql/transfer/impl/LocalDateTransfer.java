package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.Date;
import java.sql.ResultSet;

public class LocalDateTransfer implements ResultSetTransfer
{
    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        Date date = resultSet.getDate(columnIndex);
        if (date != null)
        {
            return date.toLocalDate();
        }
        else
        {
            return null;
        }
    }
}
