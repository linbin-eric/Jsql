package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.util.Map;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class EnumOrdinalTransfer extends AbstractResultsetTransfer
{
    Enum<?>[] instances;
    
    @Override
    protected Enum<?> valueOf(ResultSet resultSet, String sql) throws Exception
    {
        int result = resultSet.getInt(1);
        return resultSet.wasNull() ? null : instances[result];
    }
    
    @Override
    public void initialize(Class<?> type, JdbcTypeDictionary jdbcTypeDictionary)
    {
        @SuppressWarnings("unchecked")
        Map<String, ? extends Enum<?>> allEnumInstances = ReflectUtil.getAllEnumInstances((Class<? extends Enum<?>>) type);
        instances = new Enum<?>[allEnumInstances.size()];
        for (Enum<?> each : allEnumInstances.values())
        {
            instances[each.ordinal()] = each;
        }
        
    }
    
}
