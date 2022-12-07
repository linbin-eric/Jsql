package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EnumOrdinalTransfer extends ColumnNameHolder
{
    private Enum<?>[] instances;

    public EnumOrdinalTransfer(String columnName)
    {
        super(columnName);
    }

    public EnumOrdinalTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        int ordinal = columnName == null ? resultSet.getInt(1) : resultSet.getInt(columnName);
        if (resultSet.wasNull())
        {
            return null;
        }
        return instances[ordinal];
    }

    @Override
    public ResultSetTransfer awareType(Class type)
    {
        instances = (Enum<?>[]) type.getEnumConstants();
        return this;
    }
}
