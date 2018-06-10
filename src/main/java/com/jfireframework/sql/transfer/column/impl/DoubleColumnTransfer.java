package com.jfireframework.sql.transfer.column.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DoubleColumnTransfer extends AbstractColumnTransfer
{
    
    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException, IllegalArgumentException, IllegalAccessException
    {
        double value = resultSet.getDouble(columnName);
        if (resultSet.wasNull() == false)
        {
            field.set(entity, value);
        }
    }
    
}
