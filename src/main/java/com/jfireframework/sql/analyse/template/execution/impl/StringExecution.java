package com.jfireframework.sql.analyse.template.execution.impl;

import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.analyse.template.execution.Execution;

public class StringExecution implements Execution
{
    private String literals;
    
    public StringExecution(String literals)
    {
        this.literals = literals;
    }
    
    @Override
    public boolean execute(Map<String, Object> variables, StringCache cache, List<Object> params)
    {
        cache.append(literals);
        return true;
    }
    
    @Override
    public void check()
    {
        
    }
    
}
