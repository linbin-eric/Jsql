package com.jfireframework.sql.resultsettransfer.field.impl;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;

public class SqlDateField extends AbstractMapField
{
    public SqlDateField(Field field, ColNameStrategy colNameStrategy)
    {
        super(field, colNameStrategy);
    }
    
    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException
    {
        Date date = resultSet.getDate(dbColName);
        unsafe.putObject(entity, offset, date);
    }
    
    @Override
    public Object fieldValue(Object entity)
    {
        return unsafe.getObject(entity, offset);
    }
    
}
