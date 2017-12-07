package com.jfireframework.sql.transfer.resultset.impl;

import java.sql.ResultSet;
import java.util.Map;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.SessionfactoryConfig;

public class EnumOrdinalTransfer extends AbstractResultsetTransfer<Enum<?>>
{
	Enum<?>[] instances;
	
	@Override
	protected Enum<?> valueOf(ResultSet resultSet) throws Exception
	{
		int result = resultSet.getInt(1);
		return resultSet.wasNull() ? null : instances[result];
	}
	
	@Override
	public void initialize(Class<Enum<?>> type, SessionfactoryConfig config)
	{
		Map<String, ? extends Enum<?>> allEnumInstances = ReflectUtil.getAllEnumInstances((Class<? extends Enum<?>>) type);
		instances = new Enum<?>[allEnumInstances.size()];
		for (Enum<?> each : allEnumInstances.values())
		{
			instances[each.ordinal()] = each;
		}
		
	}
	
}
