package com.jfireframework.sql.transfer.column.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IntOperator extends AbstractFieldOperator
{
    
    @Override
    public void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException
    {
        int value = resultSet.getInt(dbColName);
        if (resultSet.wasNull() == false)
        {
            unsafe.putInt(entity, offset, value);
        }
    }
    
    
}
