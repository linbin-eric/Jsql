package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FloatTransfer extends ColumnIndexHolder
{
    public FloatTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public FloatTransfer()
    {
        super(1);
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        float f = resultSet.getFloat(columnIndex);
        if (resultSet.wasNull())
        {
            return null;
        }
        return f;
    }
}
