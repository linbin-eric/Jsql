package com.jfireframework.sql.transfer.column.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BooleanColumnTransfer extends AbstractColumnTransfer
{
    
    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException, IllegalArgumentException, IllegalAccessException
    {
        boolean b = resultSet.getBoolean(columnName);
        if (resultSet.wasNull() == false)
        {
            field.set(entity, b);
        }
    }
    
}
