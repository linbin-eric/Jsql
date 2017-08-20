package com.jfireframework.sql.mapfield.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DoubleOperator extends AbstractFieldOperator
{
    
    @Override
    public void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException
    {
        double value = resultSet.getDouble(dbColName);
        if (resultSet.wasNull() == false)
        {
            unsafe.putDouble(entity, offset, value);
        }
    }
    
    @Override
    public Object fieldValue(Object entity)
    {
        return unsafe.getDouble(entity, offset);
    }
    
}
