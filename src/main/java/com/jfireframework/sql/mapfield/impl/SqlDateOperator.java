package com.jfireframework.sql.mapfield.impl;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlDateOperator extends AbstractFieldOperator
{
    
    @Override
    public void setEntityValue(Object entity, Field field, String dbColName, long offset, ResultSet resultSet) throws SQLException
    {
        Date date = resultSet.getDate(dbColName);
        unsafe.putObject(entity, offset, date);
    }
    
    @Override
    public Object fieldValue(Object entity, Field field, long offset)
    {
        return unsafe.getObject(entity, offset);
    }
    
}
