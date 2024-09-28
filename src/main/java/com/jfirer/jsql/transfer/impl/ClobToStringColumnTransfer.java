package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.Clob;
import java.sql.ResultSet;

public class ClobToStringColumnTransfer implements ResultSetTransfer
{
    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        Clob clob = resultSet.getClob(columnIndex);
        if (clob != null)
        {
            return clob.getSubString(1, (int) clob.length());
        }
        else
        {
            return null;
        }
    }
}
