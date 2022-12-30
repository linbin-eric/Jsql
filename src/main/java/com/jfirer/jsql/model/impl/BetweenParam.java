package com.jfirer.jsql.model.impl;

import com.jfirer.jsql.model.BaseModel;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.support.SFunction;

import java.util.List;

public class BetweenParam extends InternalParamImpl
{
    public BetweenParam(SFunction<?, ?> fn, Object value1, Object value2)
    {
        super(fn);
        consumer = (columnName, builder, paramValues) -> {
            builder.append(columnName).append(" between ");
            putValue(value1, builder, paramValues);
            builder.append(" and ");
            putValue(value2, builder, paramValues);
        };
    }

    private void putValue(Object value, StringBuilder builder, List<Object> paramValues)
    {
        if (value instanceof Model m)
        {
            BaseModel.ModelResult result = m.getResult();
            builder.append(" ( ").append(result.sql()).append(" )");
            paramValues.addAll(result.paramValues());
        }
        else
        {
            builder.append(" ? ");
            paramValues.add(value);
        }
    }
}
