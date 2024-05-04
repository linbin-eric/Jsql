package com.jfirer.jsql.model.param;

import com.jfirer.jsql.model.support.SFunction;

public class NotNullValueParam extends InternalParamImpl
{
    public NotNullValueParam(SFunction<?, ?> fn)
    {
        super(fn);
        consumer = (columnName, builder, paramValues) -> {
            builder.append(columnName).append(" is not null ");
        };
    }
}
