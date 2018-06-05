package com.jfireframework.sql.dialect.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.jfireframework.sql.dialect.Dialect;

public class OracleDialect implements Dialect
{
	
	@Override
	public void fillStatement(PreparedStatement preparedStatement, List<Object> params) throws SQLException
	{
		int index = 1;
		for (Object value : params)
		{
			if (value instanceof java.sql.Date)
			{
				preparedStatement.setDate(index, (java.sql.Date) value);
			}
			else if (value instanceof java.sql.Timestamp)
			{
				preparedStatement.setTimestamp(index, (java.sql.Timestamp) value);
			}
			else if (value instanceof java.util.Date)
			{
				Date date = (Date) value;
				preparedStatement.setTimestamp(index, new Timestamp(date.getTime()));
			}
			else if (value instanceof Calendar)
			{
				Calendar calendar = (Calendar) value;
				preparedStatement.setTimestamp(index, new Timestamp(calendar.getTimeInMillis()));
			}
			else
			{
				setUnDefinedType(preparedStatement, index, value);
			}
			index++;
		}
	}
	
	protected void setUnDefinedType(PreparedStatement preparedStatement, int i, Object value) throws SQLException
	{
		preparedStatement.setObject(i, value);
	}
}
