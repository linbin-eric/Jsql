package com.jfirer.jsql.transfer;

import java.sql.ResultSet;

public interface ResultSetTransfer
{
    default Object transfer(ResultSet resultSet)
    {
        return transfer(resultSet, 1);
    }

    Object transfer(ResultSet resultSet, int columnIndex);

    default void awareType(Class type)
    {
    }
}
