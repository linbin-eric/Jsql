package cc.jfire.jsql.transfer.impl;

import cc.jfire.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class DoubleTransfer implements ResultSetTransfer
{
    public static final DoubleTransfer INSTANCE = new DoubleTransfer();

    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        double d = resultSet.getDouble(columnIndex);
        if (resultSet.wasNull())
        {
            return null;
        }
        return d;
    }
}
