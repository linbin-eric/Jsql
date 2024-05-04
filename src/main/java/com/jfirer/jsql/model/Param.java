package com.jfirer.jsql.model;

import com.jfirer.jsql.model.param.*;
import com.jfirer.jsql.model.support.SFunction;

import java.util.function.Supplier;

public interface Param
{
    Param and(Param param);

    default Param ifAnd(Supplier<Boolean> supplier, Param param)
    {
        if (supplier.get())
        {
            return this.and(param);
        }
        else
        {
            return this;
        }
    }

    Param or(Param param);

    default Param ifOr(Supplier<Boolean> supplier, Param param)
    {
        if (supplier.get())
        {
            return this.or(param);
        }
        else
        {
            return this;
        }
    }

    Param union();

    static <T> Param isNull(SFunction<T, ?> fn)
    {
        return new NullValueParam(fn);
    }

    static <T> Param notNull(SFunction<T, ?> fn)
    {
        return new NotNullValueParam(fn);
    }

    static <T> Param eq(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, "=");
    }

    static <T, R> Param eq(SFunction<T, ?> fn1, SFunction<R, ?> fn2)
    {
        return new TwoFieldEqParam(fn1, fn2);
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
