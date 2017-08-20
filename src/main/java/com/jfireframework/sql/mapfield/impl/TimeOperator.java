package com.jfireframework.sql.mapfield.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TimeOperator extends AbstractFieldOperator
{
    
    @Override
    public void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException
    {
        unsafe.putObject(entity, offset, resultSet.getTime(dbColName));
    }
    
    @Override
    public Object fieldValue(Object entity)
    {
        return unsafe.getObject(entity, offset);
    }
    
}
