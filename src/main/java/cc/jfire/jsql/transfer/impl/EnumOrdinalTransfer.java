package cc.jfire.jsql.transfer.impl;

import cc.jfire.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class EnumOrdinalTransfer implements ResultSetTransfer
{
    private Enum<?>[] instances;

    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        int ordinal = resultSet.getInt(columnIndex);
        if (resultSet.wasNull())
        {
            return null;
        }
        return instances[ordinal];
    }

    @Override
    public void awareType(Class type)
    {
        instances = (Enum<?>[]) type.getEnumConstants();
    }
}
