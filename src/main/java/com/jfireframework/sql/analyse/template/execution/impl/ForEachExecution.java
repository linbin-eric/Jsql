package com.jfireframework.sql.analyse.template.execution.impl;

import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.jfireel.expression.Expression;
import com.jfireframework.sql.analyse.template.execution.Execution;
import com.jfireframework.sql.analyse.template.execution.WithBodyExecution;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ForEachExecution implements WithBodyExecution
{
    private Execution[] body;
    private final String itemName;
    private final Expression collection;

    public ForEachExecution(String itemName, Expression collection)
    {
        this.itemName = itemName;
        this.collection = collection;
    }

    @Override
    public boolean execute(Map<String, Object> variables, StringCache cache, List<Object> params)
    {
        Object result = collection.calculate(variables);
        if ( result == null )
        {
            return true;
        }
        if ( result instanceof Collection<?> )
        {
            for (Object each : ((Collection<?>) result))
            {
                variables.put(itemName, each);
                for (Execution execution : body)
                {
                    execution.execute(variables, cache, params);
                }
            }
            variables.remove(itemName);
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
