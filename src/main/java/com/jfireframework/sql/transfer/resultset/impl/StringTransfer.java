package com.jfireframework.sql.transfer.resultset.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

public class StringTransfer implements ResultSetTransfer
{
	
	@Override
	public Object transfer(ResultSet resultSet) throws SQLException
	{
		return resultSet.getString(1);
	}
	
	@Override
	public ResultSetTransfer initialize(Class<?> type)
	{
		return this;
	}
	
}
