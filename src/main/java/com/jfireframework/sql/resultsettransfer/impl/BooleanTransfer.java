package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;

public class BooleanTransfer extends AbstractResultsetTransfer<Boolean>
{
    
    public BooleanTransfer(Class<?> ckass)
    {
        super(ckass);
    }
    
    @Override
    protected Boolean valueOf(ResultSet resultSet, String sql) throws Exception
    {
        return Boolean.valueOf(resultSet.getBoolean(1));
    }
    
}
