package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringTransfer extends ColumnNameHolder
{
    public StringTransfer(String columnName)
    {
        super(columnName);
    }

    public StringTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        return columnName == null ? resultSet.getString(1) : resultSet.getString(columnName);
    }
}
