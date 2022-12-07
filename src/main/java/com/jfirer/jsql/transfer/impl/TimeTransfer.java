package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TimeTransfer extends ColumnNameHolder
{
    public TimeTransfer(String columnName)
    {
        super(columnName);
    }

    public TimeTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        return columnName == null ? resultSet.getTime(1) : resultSet.getTime(columnName);
    }
}
