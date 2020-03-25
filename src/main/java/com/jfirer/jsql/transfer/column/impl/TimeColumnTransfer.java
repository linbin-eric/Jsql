package com.jfirer.jsql.transfer.column.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

public class TimeColumnTransfer extends AbstractColumnTransfer
{

    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException, IllegalArgumentException, IllegalAccessException
    {
        Time time = resultSet.getTime(columnName);
        if ( time != null )
        {
            field.set(entity, time);
        }
    }

}
