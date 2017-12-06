package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import com.jfireframework.sql.SessionfactoryConfig;

public class UtilDateTransfer extends AbstractResultsetTransfer<Date>
{
	
	@Override
	protected Date valueOf(ResultSet resultSet) throws Exception
	{
		Timestamp timestamp = resultSet.getTimestamp(1);
		return new Date(timestamp.getTime());
	}
	
	@Override
	public void initialize(Class<Date> type, SessionfactoryConfig config)
	{
	}
	
}
