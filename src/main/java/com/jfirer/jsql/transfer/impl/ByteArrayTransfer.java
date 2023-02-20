package com.jfirer.jsql.transfer.impl;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ByteArrayTransfer extends ColumnIndexHolder
{
    public ByteArrayTransfer()
    {
        super(1);
    }

    public ByteArrayTransfer(int columnIndex)
    {
        super(columnIndex);
    }

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        Blob blob = resultSet.getBlob(columnIndex);
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
