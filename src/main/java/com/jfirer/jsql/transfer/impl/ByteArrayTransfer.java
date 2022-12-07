package com.jfirer.jsql.transfer.impl;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ByteArrayTransfer extends ColumnNameHolder
{
    public ByteArrayTransfer()
    {
    }

    public ByteArrayTransfer(String columnName)
    {
        super(columnName);
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        Blob blob = columnName == null ? resultSet.getBlob(1) : resultSet.getBlob(columnName);
        if (blob != null)
        {
            byte[] array = blob.getBytes(1, (int) blob.length());
            blob.free();
            return array;
        }
        else
        {
            return null;
        }
    }
}
