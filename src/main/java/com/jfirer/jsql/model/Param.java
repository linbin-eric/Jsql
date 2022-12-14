package com.jfirer.jsql.model;

import com.jfirer.jsql.model.impl.BetweenParam;
import com.jfirer.jsql.model.impl.InParam;
import com.jfirer.jsql.model.impl.OneValueParam;
import com.jfirer.jsql.model.impl.StringPatternParam;
import com.jfirer.jsql.model.support.SFunction;

public interface Param
{
    Param and(Param param);

    Param or(Param param);

    Param union();

    static <T> Param eq(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, "=");
    }

    static <T> Param notEq(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, "!=");
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

    static <T> Param startWith(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.AFTER, value);
    }

    static <T> Param endWith(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.BEFORE, value);
    }

    static <T> Param like(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.NONE, value);
    }

    static <T> Param contain(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.BOTH, value);
    }

    static <T> Param in(SFunction<T, ?> fn, Object... values)
    {
        return new InParam(fn, InParam.IN, values);
    }

    static <T> Param notIn(SFunction<T, ?> fn, Object... values)
    {
        return new InParam(fn, InParam.NOT_IN, values);
    }
}
