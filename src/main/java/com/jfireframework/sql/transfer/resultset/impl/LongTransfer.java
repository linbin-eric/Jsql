package com.jfireframework.sql.transfer.resultset.impl;

import java.sql.ResultSet;
import com.jfireframework.sql.SessionfactoryConfig;

public class LongTransfer extends AbstractResultsetTransfer<Long>
{
	
	@Override
	protected Long valueOf(ResultSet resultSet) throws Exception
	{
		return Long.valueOf(resultSet.getLong(1));
	}
	
	@Override
	public void initialize(Class<Long> type, SessionfactoryConfig config)
	{
	}
	
}
