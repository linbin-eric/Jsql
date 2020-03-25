package com.jfirer.jsql.transfer.column.impl;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlDateColumnTransfer extends AbstractColumnTransfer
{

    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException, IllegalArgumentException, IllegalAccessException
    {
        Date date = resultSet.getDate(columnName);
        if ( date != null )
        {
            field.set(entity, date);
        }
    }

}
