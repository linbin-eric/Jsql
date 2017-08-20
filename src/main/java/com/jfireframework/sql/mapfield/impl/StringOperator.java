package com.jfireframework.sql.mapfield.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringOperator extends AbstractFieldOperator
{
    
    @Override
    public void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException
    {
        String value = resultSet.getString(dbColName);
        if (resultSet.wasNull())
        {
            unsafe.putObject(entity, offset, null);
        }
        else
        {
            unsafe.putObject(entity, offset, value);
        }
    }
    
    @Override
    public Object fieldValue(Object entity)
    {
        return unsafe.getObject(entity, offset);
    }
    
}
