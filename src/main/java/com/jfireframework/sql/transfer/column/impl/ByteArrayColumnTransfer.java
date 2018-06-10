package com.jfireframework.sql.transfer.column.impl;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ByteArrayColumnTransfer extends AbstractColumnTransfer
{
    
    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException, IllegalArgumentException, IllegalAccessException
    {
        Blob blob = resultSet.getBlob(columnName);
        if (blob != null)
        {
            byte[] array = blob.getBytes(1, (int) blob.length());
            blob.free();
            field.set(entity, array);
        }
    }
    
}
