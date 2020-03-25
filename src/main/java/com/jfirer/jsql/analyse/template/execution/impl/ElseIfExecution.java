package com.jfirer.jsql.analyse.template.execution.impl;

import com.jfirer.jsql.analyse.template.execution.Execution;
import com.jfirer.jsql.analyse.template.execution.WithBodyExecution;
import com.jfirer.jfireel.expression.Expression;

import java.util.List;
import java.util.Map;

public class ElseIfExecution implements WithBodyExecution
{
    private final Expression  expression;
    private       Execution[] body;

    public ElseIfExecution(Expression expression)
    {
        this.expression = expression;
    }

    @Override
    public boolean execute(Map<String, Object> variables, StringBuilder cache, List<Object> params)
    {
        if ( expression.calculate(variables) )
        {
            for (Execution each : body)
            {
                each.execute(variables, cache, params);
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void check()
    {
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
