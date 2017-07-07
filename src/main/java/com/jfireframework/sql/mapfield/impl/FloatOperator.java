package com.jfireframework.sql.mapfield.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FloatOperator extends AbstractFieldOperator
{
    
    @Override
    public void setEntityValue(Object entity, Field field, String dbColName, long offset, ResultSet resultSet) throws SQLException
    {
        float value = resultSet.getFloat(dbColName);
        if (resultSet.wasNull() == false)
        {
            unsafe.putFloat(entity, offset, value);
        }
    }
    
    @Override
    public Object fieldValue(Object entity, Field field, long offset)
    {
        return unsafe.getFloat(entity, offset);
    }
    
}
