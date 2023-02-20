package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BigDecimalTransfer extends ColumnNameHolder
{
    public BigDecimalTransfer(String columnName)
    {
        super(columnName);
    }

    public BigDecimalTransfer()
    {
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        return columnName == null ? resultSet.getBigDecimal(1) : resultSet.getBigDecimal(columnName);
    }
}
