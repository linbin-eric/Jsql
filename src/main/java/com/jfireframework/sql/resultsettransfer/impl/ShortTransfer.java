package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import com.jfireframework.sql.SessionfactoryConfig;

public class ShortTransfer extends AbstractResultsetTransfer
{
    
    @Override
    protected Short valueOf(ResultSet resultSet) throws Exception
    {
        return Short.valueOf(resultSet.getShort(1));
    }
    
    @Override
    public void initialize(Class<?> type, SessionfactoryConfig config)
    {
    }
    
}
