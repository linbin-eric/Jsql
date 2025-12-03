package cc.jfire.jsql.model.param;

import cc.jfire.jsql.model.support.SFunction;

public class NullValueParam extends InternalParamImpl
{
    public NullValueParam(SFunction<?, ?> fn)
    {
        super(fn);
        consumer = (columnName, builder, paramValues) -> {
            builder.append(columnName).append(" is null ");
        };
    }
}
