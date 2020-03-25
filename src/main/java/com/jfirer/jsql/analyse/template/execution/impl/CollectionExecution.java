package com.jfirer.jsql.analyse.template.execution.impl;

import com.jfirer.jsql.analyse.template.execution.Execution;
import com.jfirer.jfireel.expression.Expression;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CollectionExecution implements Execution
{
    private final Expression expression;

    public CollectionExecution(Expression expression)
    {
        this.expression = expression;
    }

    @Override
    public boolean execute(Map<String, Object> variables, StringBuilder cache, List<Object> params)
    {
        Object result = expression.calculate(variables);
        if (result instanceof Collection<?>)
        {
            cache.append("(");
            for (Object each : (Collection<?>) result)
            {
                cache.append("?,");
                params.add(each);
            }
            cache.setLength(cache.length() - 1);
            cache.append(") ");
        }
        else if (result instanceof String)
        {
            String[] split = ((String) result).split(",");
            cache.append("(");
            for (String each : split)
            {
                cache.append("?,");
                params.add(each);
            }
            if (split.length != 0)
            {
                cache.setLength(cache.length() - 1);
            }
            cache.append(") ");
        }
        else if (result.getClass().isArray())
        {
            if (result.getClass().getComponentType().isPrimitive() == false)
            {
                cache.append("(");
                for (Object each : (Object[]) result)
                {
                    cache.append("?,");
                    params.add(each);
                }
                cache.setLength(cache.length() - 1);
                cache.append(") ");
            }
            else if (result instanceof int[])
            {
                cache.append("(");
                for (int each : (int[]) result)
                {
                    cache.append("?,");
                    params.add(each);
                }
                cache.setLength(cache.length()-1);
                cache.append(") ");
            }
            else if (result instanceof boolean[])
            {
                cache.append("(");
                for (boolean each : (boolean[]) result)
                {
                    cache.append("?,");
                    params.add(each);
                }
                cache.setLength(cache.length()-1);
                cache.append(") ");
            }
            else if (result instanceof char[])
            {
                cache.append("(");
                for (char each : (char[]) result)
                {
                    cache.append("?,");
                    params.add(each);
                }
                cache.setLength(cache.length()-1);
                cache.append(") ");
            }
            else if (result instanceof byte[])
            {
                cache.append("(");
                for (byte each : (byte[]) result)
                {
                    cache.append("?,");
                    params.add(each);
                }
                cache.setLength(cache.length()-1);
                cache.append(") ");
            }
            else if (result instanceof short[])
            {
                cache.append("(");
                for (short each : (short[]) result)
                {
                    cache.append("?,");
                    params.add(each);
                }
                cache.setLength(cache.length()-1);
                cache.append(") ");
            }
            else if (result instanceof long[])
            {
                cache.append("(");
                for (long each : (long[]) result)
                {
                    cache.append("?,");
                    params.add(each);
                }
                cache.setLength(cache.length()-1);
                cache.append(") ");
            }
            else if (result instanceof float[])
            {
                cache.append("(");
                for (float each : (float[]) result)
                {
                    cache.append("?,");
                    params.add(each);
                }
                cache.setLength(cache.length()-1);
                cache.append(") ");
            }
            else if (result instanceof double[])
            {
                cache.append("(");
                for (double each : (double[]) result)
                {
                    cache.append("?,");
                    params.add(each);
                }
                cache.setLength(cache.length()-1);
                cache.append(") ");
            }
        }
        else
        {
            throw new IllegalArgumentException("参数不正确，应该放入集合或者数组，请检查" + expression.getEl());
        }
        return true;
    }

    @Override
    public void check()
    {
        // TODO Auto-generated method stub
    }
}
