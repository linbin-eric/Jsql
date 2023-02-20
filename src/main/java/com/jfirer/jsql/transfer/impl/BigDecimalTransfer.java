package com.jfirer.jsql.transfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BigDecimalTransfer extends ColumnIndexHolder
{
    public BigDecimalTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    public BigDecimalTransfer()
    {
        super(1);
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        return resultSet.getBigDecimal(columnIndex);
    }
}
