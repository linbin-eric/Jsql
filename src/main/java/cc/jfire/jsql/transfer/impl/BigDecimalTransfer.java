package cc.jfire.jsql.transfer.impl;

import cc.jfire.jsql.transfer.ResultSetTransfer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class BigDecimalTransfer implements ResultSetTransfer
{
    public static final BigDecimalTransfer INSTANCE = new BigDecimalTransfer();

    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int columnIndex)
    {
        return resultSet.getBigDecimal(columnIndex);
    }
}
