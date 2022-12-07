package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LongTransfer extends ColumnIndexHolder
{

    public LongTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public LongTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        long l = resultSet.getLong(columnIndex);
        if (resultSet.wasNull())
        {
            return null;
        }
        return l;
    }
}
