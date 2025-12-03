package cc.jfire.jsql.transfer.impl;

import cc.jfire.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class FloatTransfer implements ResultSetTransfer
{
    public static final FloatTransfer INSTANCE = new FloatTransfer();

    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        float f = resultSet.getFloat(columnIndex);
        if (resultSet.wasNull())
        {
            return null;
        }
        return f;
    }
}
