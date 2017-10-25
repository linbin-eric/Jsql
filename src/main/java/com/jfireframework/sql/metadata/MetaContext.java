package com.jfireframework.sql.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.annotation.NameStrategy;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.dbstructure.name.ColumnNameStrategy;
import com.jfireframework.sql.dbstructure.name.DefaultNameStrategy;

public class MetaContext
{
    private final Map<String, TableMetaData>                             entityMap = new HashMap<String, TableMetaData>();
    private final TableMetaData[]                                        metaDatas;
    private final Map<Class<? extends ColumnNameStrategy>, ColumnNameStrategy> map       = new HashMap<Class<? extends ColumnNameStrategy>, ColumnNameStrategy>();
    
    public MetaContext(Set<Class<?>> set, SessionfactoryConfig config)
    {
        for (Class<?> each : set)
        {
            if (each.isAnnotationPresent(TableEntity.class))
            {
                TableMetaData tableMetaData = new TableMetaData(each, getColNameStrategy(each), config);
                entityMap.put(each.getSimpleName(), tableMetaData);
            }
        }
        metaDatas = entityMap.values().toArray(new TableMetaData[entityMap.size()]);
    }
    
    private ColumnNameStrategy getColNameStrategy(Class<?> entityClass)
    {
        Class<? extends ColumnNameStrategy> ckass = entityClass.isAnnotationPresent(NameStrategy.class) ? entityClass.getAnnotation(NameStrategy.class).value() : DefaultNameStrategy.class;
        ColumnNameStrategy nameStrategy = map.get(ckass);
        if (nameStrategy == null)
        {
            try
            {
                nameStrategy = ckass.newInstance();
                map.put(ckass, nameStrategy);
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        return nameStrategy;
    }
    
    public TableMetaData get(String entityClassSimpleName)
    {
        return entityMap.get(entityClassSimpleName);
    }
    
    public TableMetaData[] metaDatas()
    {
        return metaDatas;
    }
}
