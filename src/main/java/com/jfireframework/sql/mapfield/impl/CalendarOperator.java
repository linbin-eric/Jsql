package com.jfireframework.sql.mapfield.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

public class CalendarOperator extends AbstractFieldOperator
{
    
    @Override
    public void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException
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
        return ((Calendar) unsafe.getObject(entity, offset)).getTime();
    }
    
}
