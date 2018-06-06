package com.jfireframework.sql.transfer.column.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

public class CalendarColumnTransfer extends AbstractColumnTransfer
{
	
	@Override
	public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException, IllegalArgumentException, IllegalAccessException
	{
		Timestamp timestamp = resultSet.getTimestamp(columnName);
		if (timestamp != null)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(timestamp.getTime());
			field.set(entity, calendar);
		}
	}
	
}
