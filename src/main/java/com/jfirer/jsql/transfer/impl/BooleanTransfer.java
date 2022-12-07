package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BooleanTransfer extends ColumnIndexHolder
{
    public BooleanTransfer()
    {
    }

    public BooleanTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        boolean b = resultSet.getBoolean(columnIndex);
        if (resultSet.wasNull())
        {
            return null;
        }
        return b;
    }
}
