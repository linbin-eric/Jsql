package com.jfirer.jsql.model.impl;

import com.jfirer.jsql.model.BaseModel;
import com.jfirer.jsql.model.support.SFunction;

public class StringPatternParam extends InternalParamImpl
{
    public enum PatternMode
    {
        BEFORE,
        AFTER,
        NONE,
        BOTH
    }

    public StringPatternParam(SFunction<?, ?> fn, PatternMode mode, Object value)
    {
        super(fn);
        consumer = (columnName, builder, paramValues) -> {
            switch (mode)
            {
                case BEFORE ->
                {
                    builder.append(columnName).append(" like ?");
                    paramValues.add("%" + value);
                }
                case AFTER ->
                {
                    builder.append(columnName).append(" like ?");
                    paramValues.add(value + "%");
                }
                case NONE ->
                {
                    if (value instanceof BaseModel m)
                    {
                        builder.append(columnName).append(" like (");
                        BaseModel.ModelResult result = m.getResult();
                        builder.append(result.sql()).append(')');
                        paramValues.addAll(result.paramValues());
                    }
                    else
                    {
                        builder.append(columnName).append(" like ?");
                        paramValues.add(value);
                    }
                }
                case BOTH ->
                {
                    builder.append(columnName).append(" like ?");
                    paramValues.add("%" + value + "%");
                }
            }
        };
    }
}
