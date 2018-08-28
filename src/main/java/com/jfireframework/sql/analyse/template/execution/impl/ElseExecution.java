package com.jfireframework.sql.analyse.template.execution.impl;

import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.analyse.template.execution.Execution;
import com.jfireframework.sql.analyse.template.execution.WithBodyExecution;

import java.util.List;
import java.util.Map;

public class ElseExecution implements WithBodyExecution
{
    private Execution[] body;

    @Override
    public boolean execute(Map<String, Object> variables, StringCache cache, List<Object> params)
    {
        for (Execution each : body)
        {
            each.execute(variables, cache, params);
        }
        return true;
    }

    @Override
    public void check()
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void setBody(Execution... executions)
    {
        body = executions;
    }

    @Override
    public boolean isBodyNotSet()
    {
        return body == null;
    }
}
