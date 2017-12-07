package com.jfireframework.sql.transfer.column.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LongOperator extends AbstractFieldOperator
{
    
    @Override
    public void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException
    {
        long value = resultSet.getLong(dbColName);
        if (resultSet.wasNull() == false)
        {
            unsafe.putLong(entity, offset, value);
        }
    }
    
    
}
