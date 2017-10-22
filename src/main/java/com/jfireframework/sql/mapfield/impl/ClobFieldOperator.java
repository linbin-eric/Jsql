package com.jfireframework.sql.mapfield.impl;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClobFieldOperator extends AbstractFieldOperator
{
    
    @Override
    public void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException
    {
        Clob clob = resultSet.getClob(dbColName);
        if (clob != null)
        {
            String subString = clob.getSubString(1, (int) clob.length());
            unsafe.putObject(entity, offset, subString);
        }
    }
    
    @Override
    public Object fieldValue(Object entity)
    {
        return unsafe.getObject(entity, offset);
    }
    
}
