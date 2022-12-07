package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegerTransfer extends ColumnIndexHolder
{

    public IntegerTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public IntegerTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        int i = resultSet.getInt(columnIndex);
        if (resultSet.wasNull())
        {
            return null;
        }
        return i;
    }
}
