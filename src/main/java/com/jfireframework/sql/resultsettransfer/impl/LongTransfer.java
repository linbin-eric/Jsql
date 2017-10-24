package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import com.jfireframework.sql.SessionfactoryConfig;

public class LongTransfer extends AbstractResultsetTransfer
{
    
    @Override
    protected Long valueOf(ResultSet resultSet) throws Exception
    {
        return Long.valueOf(resultSet.getLong(1));
    }
    
    @Override
    public void initialize(Class<?> type, SessionfactoryConfig config)
    {
    }
    
}
