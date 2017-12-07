package com.jfireframework.sql.transfer.column.impl;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ByteArrayColumnTransfer extends AbstractColumnTransfer
{
	
	@Override
	public void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException
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
	
}
