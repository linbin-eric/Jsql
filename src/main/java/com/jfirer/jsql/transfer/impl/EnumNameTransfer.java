package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EnumNameTransfer extends ColumnIndexHolder
{
    @SuppressWarnings("rawtypes")
    private Class<? extends Enum> type;

    public EnumNameTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public EnumNameTransfer()
    {
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        String enumName = resultSet.getString(columnIndex);
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
