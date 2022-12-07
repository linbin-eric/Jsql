package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegerTransfer extends ColumnNameHolder
{
    public IntegerTransfer(String columnName)
    {
        super(columnName);
    }

    public IntegerTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        int i = columnName == null ? resultSet.getInt(1) : resultSet.getInt(columnName);
        if (resultSet.wasNull())
        {
            return null;
        }
        return i;
    }
}
