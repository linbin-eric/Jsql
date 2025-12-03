package cc.jfire.jsql.transfer.impl;

import cc.jfire.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class BooleanTransfer implements ResultSetTransfer
{
    public static final BooleanTransfer INSTANCE = new BooleanTransfer();

    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        boolean b = resultSet.getBoolean(columnIndex);
        if (resultSet.wasNull())
        {
            return null;
        }
        return b;
    }
}
