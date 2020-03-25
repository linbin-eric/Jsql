package com.jfirer.jsql.transfer.column.impl;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClobToStringColumnTransfer extends AbstractColumnTransfer
{
    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException, IllegalArgumentException, IllegalAccessException
    {
        Clob clob = resultSet.getClob(columnName);
        if ( clob != null )
        {
            String subString = clob.getSubString(1, (int) clob.length());
            field.set(entity, subString);
        }
    }

}
