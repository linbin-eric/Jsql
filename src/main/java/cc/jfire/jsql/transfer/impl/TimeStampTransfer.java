package cc.jfire.jsql.transfer.impl;

import cc.jfire.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class TimeStampTransfer implements ResultSetTransfer
{
    public static final TimeStampTransfer INSTANCE = new TimeStampTransfer();

    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        return resultSet.getTimestamp(columnIndex);
    }
}
