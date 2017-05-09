package com.jfireframework.sql.resultsettransfer.field.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;

public class CalendarField extends AbstractMapField
{
    
    public CalendarField(Field field, ColNameStrategy colNameStrategy)
    {
        super(field, colNameStrategy);
    }
    
    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException
    {
        Timestamp timestamp = resultSet.getTimestamp(dbColName);
        if (resultSet.wasNull())
        {
            unsafe.putObject(entity, offset, null);
        }
        else
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp.getTime());
            unsafe.putObject(entity, offset, calendar);
        }
    }
    
    @Override
    public Object fieldValue(Object entity)
    {
        return (Calendar) unsafe.getObject(entity, offset);
    }
    
}
