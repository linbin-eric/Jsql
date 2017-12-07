package com.jfireframework.sql.transfer.resultset.impl;

import java.sql.ResultSet;
import java.util.Map;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.SessionfactoryConfig;

public class EnumNameTransfer extends AbstractResultsetTransfer<Enum<?>>
{
	private Map<String, ? extends Enum<?>> instances;
	
	@Override
	protected Enum<?> valueOf(ResultSet resultSet) throws Exception
	{
		String result = resultSet.getString(1);
		return result == null ? null : instances.get(result);
	}
	
	@Override
	public void initialize(Class<Enum<?>> type, SessionfactoryConfig config)
	{
		instances = ReflectUtil.getAllEnumInstances((Class<? extends Enum<?>>) type);
	}
	
}
