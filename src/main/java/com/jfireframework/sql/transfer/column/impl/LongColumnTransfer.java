package com.jfireframework.sql.transfer.column.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LongColumnTransfer extends AbstractColumnTransfer
{
	
	@Override
	public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException, IllegalArgumentException, IllegalAccessException
	{
		long value = resultSet.getLong(columnName);
		if (resultSet.wasNull() == false)
		{
			field.setLong(entity, value);
		}
	}
	
}
