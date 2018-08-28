package com.jfireframework.sql.transfer.column.impl;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClobColumnTransfer extends AbstractColumnTransfer
{

    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException, IllegalArgumentException, IllegalAccessException
    {
        Clob clob = resultSet.getClob(columnName);
        if ( clob != null )
        {
            field.set(entity, clob);
        }
    }

}
