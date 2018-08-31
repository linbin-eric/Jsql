package com.jfireframework.sql.analyse.template.execution.impl;

import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.jfireel.expression.Expression;
import com.jfireframework.sql.analyse.template.execution.Execution;

import java.util.List;
import java.util.Map;

public class TemplateCharactersExecution implements Execution
{
    private final Expression expression;

    public TemplateCharactersExecution(Expression expression)
    {
        this.expression = expression;
    }

    @Override
    public boolean execute(Map<String, Object> variables, StringCache cache, List<Object> params)
    {
        cache.append(expression.calculate(variables));
        return true;
    }

    @Override
    public void check()
    {
        // TODO Auto-generated method stub
    }
}
