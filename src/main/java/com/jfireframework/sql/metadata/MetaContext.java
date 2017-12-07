package com.jfireframework.sql.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.annotation.TableEntity;

public class MetaContext
{
	private final Map<String, TableMetaData<?>>	entityMap	= new HashMap<String, TableMetaData<?>>();
	private final TableMetaData<?>[]			metaDatas;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MetaContext(Set<Class<?>> set, SessionfactoryConfig config)
	{
		for (Class<?> each : set)
		{
			if (each.isAnnotationPresent(TableEntity.class))
			{
				TableMetaData<?> tableMetaData = new TableMetaData(each, config);
				entityMap.put(each.getSimpleName(), tableMetaData);
			}
		}
		metaDatas = entityMap.values().toArray(new TableMetaData[entityMap.size()]);
	}
	
	public TableMetaData<?> get(String entityClassSimpleName)
	{
		return entityMap.get(entityClassSimpleName);
	}
	
	public TableMetaData<?>[] metaDatas()
	{
		return metaDatas;
	}
}
