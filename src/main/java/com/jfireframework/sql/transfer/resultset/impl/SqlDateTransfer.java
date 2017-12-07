package com.jfireframework.sql.transfer.resultset.impl;

import java.sql.Date;
import java.sql.ResultSet;
import com.jfireframework.sql.SessionfactoryConfig;

public class SqlDateTransfer extends AbstractResultsetTransfer<Date>
{
	
	@Override
	protected Date valueOf(ResultSet resultSet) throws Exception
	{
		return resultSet.getDate(1);
	}
	
	@Override
	public void initialize(Class<Date> type, SessionfactoryConfig config)
	{
	}
	
}
