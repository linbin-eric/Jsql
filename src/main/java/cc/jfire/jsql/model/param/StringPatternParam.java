package cc.jfire.jsql.model.param;

import cc.jfire.jsql.model.Model;
import cc.jfire.jsql.model.support.SFunction;

public class StringPatternParam extends InternalParamImpl
{
    public enum PatternMode
    {
        BEFORE, AFTER, NONE, BOTH
    }

    public StringPatternParam(SFunction<?, ?> fn, PatternMode mode, Object value, boolean hasNot)
    {
        super(fn);
        consumer = (columnName, builder, paramValues) -> {
            switch (mode)
            {
                case BEFORE ->
                {
                    builder.append(columnName).append(hasNot ? " not like ?" : " like ?");
                    paramValues.add("%" + value);
                }
                case AFTER ->
                {
                    builder.append(columnName).append(hasNot ? " not like ?" : " like ?");
                    paramValues.add(value + "%");
                }
                case NONE ->
                {
                    if (value instanceof Model m)
                    {
                        builder.append(columnName).append(hasNot ? " not like (" : " like (");
                        Model.ModelResult result = m.getResult();
                        builder.append(result.sql()).append(')');
                        paramValues.addAll(result.paramValues());
                    }
                    else
                    {
                        builder.append(columnName).append(hasNot ? " not like ?" : " like ?");
                        paramValues.add(value);
                    }
                }
                case BOTH ->
                {
                    builder.append(columnName).append(hasNot ? " not like ?" : " like ?");
                    paramValues.add("%" + value + "%");
                }
            }
        };
    }
}
