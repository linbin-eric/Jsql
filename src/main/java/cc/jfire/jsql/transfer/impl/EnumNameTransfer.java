package cc.jfire.jsql.transfer.impl;

import cc.jfire.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class EnumNameTransfer implements ResultSetTransfer
{
    @SuppressWarnings("rawtypes")
    private Class<? extends Enum> type;

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        String enumName = resultSet.getString(columnIndex);
        if (enumName == null)
        {
            return null;
        }
        return Enum.valueOf(type, enumName);
    }

    @Override
    public void awareType(Class type)
    {
        this.type = type;
    }
}
