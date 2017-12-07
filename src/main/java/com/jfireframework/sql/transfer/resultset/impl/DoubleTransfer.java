package com.jfireframework.sql.transfer.resultset.impl;

import java.sql.ResultSet;
import com.jfireframework.sql.SessionfactoryConfig;

public class DoubleTransfer extends AbstractResultsetTransfer<Double>
{
	
	@Override
	protected Double valueOf(ResultSet resultSet) throws Exception
	{
		return Double.valueOf(resultSet.getDouble(1));
	}
	
	@Override
	public void initialize(Class<Double> type, SessionfactoryConfig config)
	{
	}
	
}
