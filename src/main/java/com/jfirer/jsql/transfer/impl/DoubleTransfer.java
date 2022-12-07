package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DoubleTransfer extends ColumnNameHolder
{
    public DoubleTransfer(String columnName)
    {
        super(columnName);
    }

    public DoubleTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        double d = columnName == null ? resultSet.getDouble(1) : resultSet.getDouble(columnName);
        if (resultSet.wasNull())
        {
            return null;
        }
        return d;
    }
}
