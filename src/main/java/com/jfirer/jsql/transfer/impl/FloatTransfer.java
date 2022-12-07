package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FloatTransfer extends ColumnNameHolder
{
    public FloatTransfer(String columnName)
    {
        super(columnName);
    }

    public FloatTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        float f = columnName == null ? resultSet.getFloat(1) : resultSet.getFloat(columnName);
        if (resultSet.wasNull())
        {
            return null;
        }
        return f;
    }
}
