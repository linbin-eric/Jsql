package com.jfireframework.sql.transfer.column.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TimestampColumnTransfer extends AbstractColumnTransfer
{
	
	@Override
	public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException, IllegalArgumentException, IllegalAccessException
	{
		Timestamp timestamp = resultSet.getTimestamp(columnName);
		if (timestamp != null)
		{
			field.set(entity, timestamp);
		}
	}
}
