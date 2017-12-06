package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.sql.Timestamp;
import com.jfireframework.sql.SessionfactoryConfig;

public class TimeStampTransfer extends AbstractResultsetTransfer<Timestamp>
{
	
	@Override
	protected Timestamp valueOf(ResultSet resultSet) throws Exception
	{
		return resultSet.getTimestamp(1);
	}
	
	@Override
	public void initialize(Class<Timestamp> type, SessionfactoryConfig config)
	{
	}
	
}
