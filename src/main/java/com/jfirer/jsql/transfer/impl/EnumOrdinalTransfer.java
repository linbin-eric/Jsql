package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EnumOrdinalTransfer extends ColumnIndexHolder
{
    private Enum<?>[] instances;

    public EnumOrdinalTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public EnumOrdinalTransfer()
    {
        super(1);
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        int ordinal = resultSet.getInt(columnIndex);
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
