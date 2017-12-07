package com.jfireframework.sql.transfer.resultset.impl;

import java.sql.ResultSet;
import com.jfireframework.sql.SessionfactoryConfig;

public class FloatTransfer extends AbstractResultsetTransfer<Float>
{
	
	@Override
	protected Float valueOf(ResultSet resultSet) throws Exception
	{
		return Float.valueOf(resultSet.getFloat(1));
	}
	
	@Override
	public void initialize(Class<Float> type, SessionfactoryConfig config)
	{
	}
	
}
