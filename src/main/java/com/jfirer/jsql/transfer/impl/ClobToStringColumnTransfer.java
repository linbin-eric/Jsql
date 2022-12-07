package com.jfirer.jsql.transfer.impl;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClobToStringColumnTransfer extends ColumnNameHolder
{
    public ClobToStringColumnTransfer(String columnName)
    {
        super(columnName);
    }

    public ClobToStringColumnTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        Clob clob = columnName == null ? resultSet.getClob(1) : resultSet.getClob(columnName);
        if (clob != null)
        {
            return clob.getSubString(1, (int) clob.length());
        }
        else
        {
            return null;
        }
    }
}
