package com.jfireframework.sql.mapfield.impl;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClobOperator extends AbstractFieldOperator
{
    
    @Override
    public void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException
    {
        Clob clob = resultSet.getClob(dbColName);
        String string = clob.getSubString(1, (int) clob.length());
        unsafe.putObject(entity, offset, string);
    }
    
    @Override
    public Object fieldValue(Object entity)
    {
        return unsafe.getObject(entity, offset);
    }
    
}
