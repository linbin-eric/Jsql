package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EnumNameTransfer extends ColumnNameHolder
{
    @SuppressWarnings("rawtypes")
    private Class<? extends Enum> type;

    public EnumNameTransfer(String columnName)
    {
        super(columnName);
    }

    public EnumNameTransfer()
    {
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        String enumName = columnName == null ? resultSet.getString(1) : resultSet.getString(columnName);
        if (enumName == null)
        {
            return null;
        }
        return Enum.valueOf(type, enumName);
    }

    @Override
    public ResultSetTransfer awareType(Class type)
    {
        this.type = type;
        return this;
    }
}
