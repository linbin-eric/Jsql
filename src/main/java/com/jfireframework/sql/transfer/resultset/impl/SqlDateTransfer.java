package com.jfireframework.sql.transfer.resultset.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

public class SqlDateTransfer implements ResultSetTransfer
{
	
	@Override
	public Object transfer(ResultSet resultSet) throws SQLException
	{
		return resultSet.getDate(1);
	}
	
	@Override
	public ResultSetTransfer initialize(Class<?> type)
	{
		return this;
	}
	
}
