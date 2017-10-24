package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import com.jfireframework.sql.SessionfactoryConfig;

public class DoubleTransfer extends AbstractResultsetTransfer
{
    
    @Override
    protected Double valueOf(ResultSet resultSet) throws Exception
    {
        return Double.valueOf(resultSet.getDouble(1));
    }
    
    @Override
    public void initialize(Class<?> type, SessionfactoryConfig config)
    {
    }
    
}
