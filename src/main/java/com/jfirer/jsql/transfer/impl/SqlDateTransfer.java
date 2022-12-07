package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlDateTransfer extends ColumnNameHolder
{
    public SqlDateTransfer(String columnName)
    {
        super(columnName);
    }

    public SqlDateTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        return columnName == null ? resultSet.getDate(1) : resultSet.getDate(columnName);
    }
}
