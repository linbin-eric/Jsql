package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BooleanTransfer extends ColumnNameHolder
{
    public BooleanTransfer()
    {
    }

    public BooleanTransfer(String columnName)
    {
        super(columnName);
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        boolean b = columnName == null ? resultSet.getBoolean(1) : resultSet.getBoolean(columnName);
        if (resultSet.wasNull())
        {
            return null;
        }
        return b;
    }
}
