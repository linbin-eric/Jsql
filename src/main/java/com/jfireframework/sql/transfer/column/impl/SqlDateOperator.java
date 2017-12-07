package com.jfireframework.sql.transfer.column.impl;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlDateOperator extends AbstractFieldOperator
{
    
    @Override
    public void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException
    {
        Date date = resultSet.getDate(dbColName);
        unsafe.putObject(entity, offset, date);
    }
    
    
}
