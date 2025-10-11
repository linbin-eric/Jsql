package com.jfirer.jsql.model.param;

import com.jfirer.jsql.model.support.SFunction;

public class BitwiseParam extends InternalParamImpl
{
    public BitwiseParam(SFunction<?, ?> fn,long bitwise,String bitwiseOperator,long value,String valueOperator)
    {
        super(fn);
        consumer = (columnName, builder, paramValues) ->
        {
            builder.append(columnName).append(" ").append(bitwiseOperator).append(" ? ").append(valueOperator).append(" ? ");
            paramValues.add(bitwise);
            paramValues.add(value);
        };
    }
}
