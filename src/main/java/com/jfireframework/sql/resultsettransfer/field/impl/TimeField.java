package com.jfireframework.sql.resultsettransfer.field.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;

public class TimeField extends AbstractMapField
{
    
    public TimeField(Field field, ColNameStrategy colNameStrategy)
    {
        super(field, colNameStrategy);
    }
    
    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException
    {
        unsafe.putObject(entity, offset, resultSet.getTime(dbColName));
    }
    
    @Override
    public Object fieldValue(Object entity)
    {
        return unsafe.getObject(entity, offset);
    }
    
}
