package com.jfirer.jsql.model.param;

import com.jfirer.jsql.model.support.SFunction;

public class NullValueParam extends InternalParamImpl
{
    public NullValueParam(SFunction<?, ?> fn, boolean is)
    {
        super(fn);
        if (is)
        {
            consumer = (columnName, builder, paramValues) -> {
                builder.append(columnName).append(" is null ");
            };
        }
        else
        {
            consumer = (columnName, builder, paramValues) -> {
                builder.append(columnName).append(" is not null ");
            };
        }
    }
}
