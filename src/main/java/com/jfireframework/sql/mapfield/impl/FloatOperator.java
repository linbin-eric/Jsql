package com.jfireframework.sql.mapfield.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FloatOperator extends AbstractFieldOperator
{
    
    @Override
    public void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException
    {
        float value = resultSet.getFloat(dbColName);
        if (resultSet.wasNull() == false)
        {
            unsafe.putFloat(entity, offset, value);
        }
    }
    
    @Override
    public Object fieldValue(Object entity)
    {
        return unsafe.getFloat(entity, offset);
    }
    
}
