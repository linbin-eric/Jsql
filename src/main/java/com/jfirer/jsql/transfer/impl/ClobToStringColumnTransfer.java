package com.jfirer.jsql.transfer.impl;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClobToStringColumnTransfer extends ColumnIndexHolder
{
    public ClobToStringColumnTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public ClobToStringColumnTransfer()
    {
        super(1);
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        Clob clob = resultSet.getClob(columnIndex);
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
