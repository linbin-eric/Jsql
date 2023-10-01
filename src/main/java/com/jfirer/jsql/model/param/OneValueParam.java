package com.jfirer.jsql.model.param;

import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.support.SFunction;

public class OneValueParam extends InternalParamImpl
{
    public OneValueParam(SFunction<?, ?> fn, Object value, String operator)
    {
        super(fn);
        consumer = (columnName, builder, paramValues) -> {
            if (value instanceof Model m)
            {
                Model.ModelResult result = m.getResult();
                builder.append(columnName).append(operator).append(" ( ").append(result.sql()).append(" )");
                paramValues.addAll(result.paramValues());
            }
            else
            {
                builder.append(columnName).append(operator).append(" ? ");
                paramValues.add(value);
            }
        };
    }
}
