package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import com.jfireframework.sql.SessionfactoryConfig;

public class BooleanTransfer extends AbstractResultsetTransfer<Boolean>
{
	
	@Override
	protected Boolean valueOf(ResultSet resultSet) throws Exception
	{
		return Boolean.valueOf(resultSet.getBoolean(1));
	}
	
	@Override
	public void initialize(Class<Boolean> type, SessionfactoryConfig config)
	{
	}
	
}
