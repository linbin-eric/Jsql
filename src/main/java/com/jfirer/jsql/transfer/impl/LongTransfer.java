package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LongTransfer extends ColumnNameHolder
{
    public LongTransfer(String columnName)
    {
        super(columnName);
    }

    public LongTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        long l = columnName == null ? resultSet.getLong(1) : resultSet.getLong(columnName);
        if (resultSet.wasNull())
        {
            return null;
        }
        return l;
    }
}
