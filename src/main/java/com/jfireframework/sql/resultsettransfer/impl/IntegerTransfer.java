package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.sql.SessionfactoryConfig;

public class IntegerTransfer extends AbstractResultsetTransfer<Integer>
{
	
	@Override
	protected Integer valueOf(ResultSet resultSet) throws SQLException
	{
		return Integer.valueOf(resultSet.getInt(1));
	}
	
	@Override
	public void initialize(Class<Integer> type, SessionfactoryConfig config)
	{
	}
	
}
