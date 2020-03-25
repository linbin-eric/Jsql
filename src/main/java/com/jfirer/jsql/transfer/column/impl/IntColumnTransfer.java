package com.jfirer.jsql.transfer.column.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IntColumnTransfer extends AbstractColumnTransfer
{

    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException, IllegalArgumentException, IllegalAccessException
    {
        int value = resultSet.getInt(columnName);
        if ( resultSet.wasNull() == false )
        {
            field.set(entity, value);
        }
    }

}
