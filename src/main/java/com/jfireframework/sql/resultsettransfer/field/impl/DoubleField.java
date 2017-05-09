package com.jfireframework.sql.resultsettransfer.field.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;

public class DoubleField extends AbstractMapField
{
    
    public DoubleField(Field field, ColNameStrategy colNameStrategy)
    {
        super(field, colNameStrategy);
    }
    
    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException
    {
        double value = resultSet.getDouble(dbColName);
        if (resultSet.wasNull() == false)
        {
            unsafe.putDouble(entity, offset, value);
        }
    }
    
    public Object fieldValue(Object entity)
    {
        return unsafe.getDouble(entity, offset);
    }
    
}
