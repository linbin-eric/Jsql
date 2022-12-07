package com.jfirer.jsql.analyse.template.execution.impl;

import com.jfirer.jfireel.expression.Expression;
import com.jfirer.jsql.analyse.template.execution.Execution;

import java.util.List;
import java.util.Map;

public class ExpressionExecution implements Execution
{
    private final Expression expression;

    public ExpressionExecution(Expression expression)
    {
        this.expression = expression;
    }

    @Override
    public boolean execute(Map<String, Object> variables, StringBuilder cache, List<Object> params)
    {
        cache.append("?");
        params.add(expression.calculate(variables));
        return true;
    }

    @Override
    public void check()
    {
        // TODO Auto-generated method stub
    }
}
