package com.jfireframework.sql.transfer.resultset.impl;

import java.sql.ResultSet;
import java.sql.Time;
import com.jfireframework.sql.SessionfactoryConfig;

public class TimeTransfer extends AbstractResultsetTransfer<Time>
{
	
	@Override
	protected Time valueOf(ResultSet resultSet) throws Exception
	{
		return resultSet.getTime(1);
	}
	
	@Override
	public void initialize(Class<Time> type, SessionfactoryConfig config)
	{
	}
	
}
