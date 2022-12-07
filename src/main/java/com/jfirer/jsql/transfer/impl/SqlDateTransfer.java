package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlDateTransfer extends ColumnIndexHolder
{
    public SqlDateTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public SqlDateTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        return resultSet.getDate(columnIndex);
    }
}
