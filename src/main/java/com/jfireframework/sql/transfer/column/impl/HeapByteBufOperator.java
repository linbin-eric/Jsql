package com.jfireframework.sql.transfer.column.impl;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.baseutil.collection.buffer.HeapByteBuf;

public class HeapByteBufOperator extends AbstractFieldOperator
{
    
    @Override
    public void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException
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
    
    
}
