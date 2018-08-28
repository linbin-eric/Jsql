package com.jfireframework.sql.transfer.column.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class DateColumnTransfer extends AbstractColumnTransfer
{

    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException, IllegalArgumentException, IllegalAccessException
    {
        Timestamp timestamp = resultSet.getTimestamp(columnName);
        if ( timestamp != null )
        {
            field.set(entity, new Date(timestamp.getTime()));
        }
    }

}
