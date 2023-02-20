package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TimeTransfer extends ColumnIndexHolder
{
    public TimeTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public TimeTransfer()
    {
        super(1);
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        return resultSet.getTime(columnIndex);
    }
}
