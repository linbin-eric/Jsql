package com.jfirer.jsql.transfer.impl;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClobTransfer extends ColumnIndexHolder
{
    public ClobTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public ClobTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        Clob clob = resultSet.getClob(columnIndex);
        return clob;
    }
}
