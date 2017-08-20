package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.util.Map;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.SessionfactoryConfig;

public class EnumNameTransfer extends AbstractResultsetTransfer
{
    private Map<String, ? extends Enum<?>> instances;
    
    @Override
    protected Enum<?> valueOf(ResultSet resultSet, String sql) throws Exception
    {
        String result = resultSet.getString(1);
        return result == null ? null : instances.get(result);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void initialize(Class<?> type, SessionfactoryConfig config)
    {
        instances = ReflectUtil.getAllEnumInstances((Class<? extends Enum<?>>) type);
    }
    
}
