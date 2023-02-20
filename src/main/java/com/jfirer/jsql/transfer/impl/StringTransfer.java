package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringTransfer extends ColumnIndexHolder
{
    public StringTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public StringTransfer()
    {
        super(1);
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        return resultSet.getString(columnIndex);
    }
}
