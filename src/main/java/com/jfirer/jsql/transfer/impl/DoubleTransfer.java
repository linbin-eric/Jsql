package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DoubleTransfer extends ColumnIndexHolder
{
    public DoubleTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public DoubleTransfer()
    {
        super(1);
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        double d = resultSet.getDouble(columnIndex);
        if (resultSet.wasNull())
        {
            return null;
        }
        return d;
    }
}
