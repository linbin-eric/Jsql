package cc.jfire.jsql.model.param;

import cc.jfire.jsql.model.support.SFunction;

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
