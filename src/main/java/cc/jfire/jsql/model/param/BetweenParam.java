package cc.jfire.jsql.model.param;

import cc.jfire.jsql.model.Model;
import cc.jfire.jsql.model.support.SFunction;

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
            Model.ModelResult result = m.getResult();
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
