package com.jfireframework.sql.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.annotation.NameStrategy;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;
import com.jfireframework.sql.dbstructure.name.DefaultNameStrategy;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class MetaContext
{
    private final Map<String, TableMetaData>                             entityMap = new HashMap<String, TableMetaData>();
    private final TableMetaData[]                                        metaDatas;
    private final Map<Class<? extends ColNameStrategy>, ColNameStrategy> map       = new HashMap<Class<? extends ColNameStrategy>, ColNameStrategy>();
    
    public MetaContext(Set<Class<?>> set, JdbcTypeDictionary jdbcTypeDictionary)
    {
        for (Class<?> each : set)
        {
            if (each.isAnnotationPresent(TableEntity.class))
            {
                TableMetaData tableMetaData = new TableMetaData(each, getColNameStrategy(each), jdbcTypeDictionary);
                entityMap.put(each.getSimpleName(), tableMetaData);
            }
        }
        metaDatas = entityMap.values().toArray(new TableMetaData[entityMap.size()]);
    }
    
    private ColNameStrategy getColNameStrategy(Class<?> entityClass)
    {
        Class<? extends ColNameStrategy> ckass = entityClass.isAnnotationPresent(NameStrategy.class) ? entityClass.getAnnotation(NameStrategy.class).value() : DefaultNameStrategy.class;
        ColNameStrategy nameStrategy = map.get(ckass);
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
