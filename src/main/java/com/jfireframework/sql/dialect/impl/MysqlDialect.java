package com.jfireframework.sql.dialect.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import com.jfireframework.sql.dialect.Dialect;

public class MysqlDialect implements Dialect
{

	@Override
	public void fillStatement(PreparedStatement preparedStatement, Object... params) throws SQLException
	{
		for (int i = 0; i < params.length; i++)
		{
			Object value = params[i];
			if (value instanceof java.sql.Date)
			{
				preparedStatement.setDate(i + 1, (java.sql.Date) value);
			}
			else if (value instanceof java.sql.Timestamp)
			{
				preparedStatement.setTimestamp(i + 1, (java.sql.Timestamp) value);
			}
			else if (value instanceof java.util.Date)
			{
				Date date = (Date) value;
				preparedStatement.setTimestamp(i + 1, new Timestamp(date.getTime()));
			}
			else if (value instanceof Calendar)
			{
				Calendar calendar = (Calendar) value;
				preparedStatement.setTimestamp(i + 1, new Timestamp(calendar.getTimeInMillis()));
			}
			else
			{
				preparedStatement.setObject(i + 1, value);
			}
		}
	}
	
}
