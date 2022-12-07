package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShortTransfer extends ColumnNameHolder
{
    public ShortTransfer(String columnName)
    {
        super(columnName);
    }

    public ShortTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        short s = columnName == null ? resultSet.getShort(1) : resultSet.getShort(columnName);
        if (resultSet.wasNull())
        {
            return null;
        }
        return s;
    }
}
