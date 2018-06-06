package com.jfireframework.sql.transfer.resultset.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

public class ShortTransfer implements ResultSetTransfer
{
	
	@Override
	public Object transfer(ResultSet resultSet) throws SQLException
	{
		short s = resultSet.getShort(1);
		if (resultSet.wasNull())
		{
			return null;
		}
		return s;
	}
	
	@Override
	public ResultSetTransfer initialize(Class<?> type)
	{
		return this;
	}
	
}
