package com.jfirer.jsql.transfer.impl;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClobTransfer extends ColumnNameHolder
{
    public ClobTransfer(String columnName)
    {
        super(columnName);
    }

    public ClobTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        Clob clob = columnName == null ? resultSet.getClob(1) : resultSet.getClob(columnName);
        return clob;
    }
}
