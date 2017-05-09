package com.jfireframework.sql.resultsettransfer.field.impl;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.baseutil.collection.buffer.HeapByteBuf;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;

public class HeapByteBufField extends AbstractMapField
{
    
    public HeapByteBufField(Field field, ColNameStrategy colNameStrategy)
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
            unsafe.putObject(entity, offset, HeapByteBuf.wrap(array));
        }
        else
        {
            unsafe.putObject(entity, offset, null);
        }
    }
    
    public Object fieldValue(Object entity)
    {
        return unsafe.getObject(entity, offset);
    }
    
}
