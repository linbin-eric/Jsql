package com.jfireframework.sql.transfer.column.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

public class CalendarColumnTransfer extends AbstractColumnTransfer
{
	
	@Override
	public void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException
	{
		Timestamp timestamp = resultSet.getTimestamp(dbColName);
		if (resultSet.wasNull())
		{
			unsafe.putObject(entity, offset, null);
		}
		else
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(timestamp.getTime());
			unsafe.putObject(entity, offset, calendar);
		}
	}
	
}
