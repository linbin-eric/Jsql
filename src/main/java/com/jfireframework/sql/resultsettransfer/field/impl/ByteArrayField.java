package com.jfireframework.sql.resultsettransfer.field.impl;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;

public class ByteArrayField extends AbstractMapField
{
    
    public ByteArrayField(Field field, ColNameStrategy colNameStrategy)
    {
        super(field, colNameStrategy);
    }
    
    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException
    {
        Blob blob = resultSet.getBlob(dbColName);
        if (blob != null)
        {
            byte[] array = blob.getBytes(1, (int) blob.length());
            blob.free();
            unsafe.putObject(entity, offset, array);
        }
        else
        {
            unsafe.putObject(entity, offset, null);
        }
    }
    
    @Override
    public Object fieldValue(Object entity)
    {
        return (byte[]) unsafe.getObject(entity, offset);
    }
    
}
