package com.jfirer.jsql.model;

import com.jfirer.jsql.model.support.SFunction;
import com.jfirer.jsql.model.impl.BetweenParam;
import com.jfirer.jsql.model.impl.InParam;
import com.jfirer.jsql.model.impl.OneValueParam;

public interface Param
{
    Param and(Param param);

    Param or(Param param);

    Param union();

    static <T> Param eq(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, "=");
    }

    static <T> Param bt(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, ">");
    }

    static <T> Param lt(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, "<");
    }

    static <T> Param be(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, ">=");
    }

    static <T> Param le(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, "<=");
    }

    static <T> Param between(SFunction<T, ?> fn, Object value1, Object value2)
    {
        return new BetweenParam(fn, value1, value2);
    }

    static <T> Param in(SFunction<T, ?> fn, Object... values)
    {
        return new InParam(fn, values);
    }
}
