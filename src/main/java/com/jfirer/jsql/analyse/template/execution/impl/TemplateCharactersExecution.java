package com.jfirer.jsql.analyse.template.execution.impl;

import com.jfirer.jsql.analyse.template.execution.Execution;
import com.jfirer.jfireel.expression.Expression;

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
    public boolean execute(Map<String, Object> variables, StringBuilder cache, List<Object> params)
    {
        Object calculate = expression.calculate(variables);
        if (calculate instanceof StringBuffer buffer)
        {
            cache.append(((StringBuffer) calculate));
        }
        else if (calculate instanceof String s)
        {
            cache.append(((String) calculate));
        }
        else if (calculate instanceof char[] array)
        {
            cache.append(((char[]) calculate));
        }
        else{
            cache.append(calculate.toString());
        }
        return true;
    }

    @Override
    public void check()
    {
        // TODO Auto-generated method stub
    }
}
