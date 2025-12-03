package cc.jfire.jsql.transfer.impl;

import cc.jfire.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class ShortTransfer implements ResultSetTransfer
{
    public static final ShortTransfer INSTANCE = new ShortTransfer();

    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        short s = resultSet.getShort(columnIndex);
        if (resultSet.wasNull())
        {
            return null;
        }
        return s;
    }
}
