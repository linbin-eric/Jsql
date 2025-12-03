package cc.jfire.jsql.transfer.impl;

import cc.jfire.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.Date;
import java.sql.ResultSet;

public class LocalDateTransfer implements ResultSetTransfer
{
    public static final LocalDateTransfer INSTANCE = new LocalDateTransfer();

    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        Date date = resultSet.getDate(columnIndex);
        if (date != null)
        {
            return date.toLocalDate();
        }
        else
        {
            return null;
        }
    }
}
