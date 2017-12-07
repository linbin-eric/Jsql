package com.jfireframework.sql.transfer.resultset.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.sql.SessionfactoryConfig;

public class StringTransfer extends AbstractResultsetTransfer<String>
{
	
	@Override
	protected String valueOf(ResultSet resultSet) throws SQLException
	{
		return resultSet.getString(1);
	}
	
	@Override
	public void initialize(Class<String> type, SessionfactoryConfig config)
	{
	}
	
}
