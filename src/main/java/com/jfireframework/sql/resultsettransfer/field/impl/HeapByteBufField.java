package com.jfireframework.sql.resultsettransfer.field.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import com.jfireframework.baseutil.collection.buffer.HeapByteBuf;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.dbstructure.ColNameStrategy;

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
    
    @Override
    public void setStatementValue(PreparedStatement statement, Object entity, int index) throws SQLException
    {
        HeapByteBuf buf = (HeapByteBuf) unsafe.getObject(entity, offset);
        if (buf == null)
        {
            statement.setNull(index, Types.BLOB);
        }
        else
        {
            Blob blob = statement.getConnection().createBlob();
            OutputStream outputStream = blob.setBinaryStream(1);
            try
            {
                outputStream.write(buf.directArray(), buf.readIndex(), buf.remainRead());
                outputStream.close();
            }
            catch (IOException e)
            {
                throw new JustThrowException(e);
            }
            statement.setBlob(index, blob);
        }
    }
    
}
