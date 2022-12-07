package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShortTransfer extends ColumnIndexHolder
{
    public ShortTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public ShortTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        short s = resultSet.getShort(columnIndex);
        if (resultSet.wasNull())
        {
            return null;
        }
        return s;
    }
}
