package com.jfirer.jsql.model;

import com.jfirer.jsql.model.param.*;
import com.jfirer.jsql.model.support.SFunction;

import java.util.Arrays;
import java.util.Collection;
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

    static <T> Param isNull(boolean condition, SFunction<T, ?> fn)
    {
        if (condition)
        {
            return new NullValueParam(fn);
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param notNull(SFunction<T, ?> fn)
    {
        return new NotNullValueParam(fn);
    }

    static <T> Param notNull(boolean condition, SFunction<T, ?> fn)
    {
        if (condition)
        {
            return new NotNullValueParam(fn);
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param eq(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, "=");
    }

    static <T> Param eq(boolean condition, SFunction<T, ?> fn, Object value)
    {
        if (condition)
        {
            return new OneValueParam(fn, value, "=");
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T, R> Param eq(SFunction<T, ?> fn1, SFunction<R, ?> fn2)
    {
        return new TwoFieldParam(fn1, fn2, " = ");
    }

    static <T, R> Param eq(boolean condition, SFunction<T, ?> fn1, SFunction<R, ?> fn2)
    {
        if (condition)
        {
            return new TwoFieldParam(fn1, fn2, " = ");
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param notEq(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, "!=");
    }

    static <T> Param notEq(boolean condition, SFunction<T, ?> fn, Object value)
    {
        if (condition)
        {
            return new OneValueParam(fn, value, "!=");
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T, R> Param notEq(SFunction<T, ?> fn1, SFunction<R, ?> fn2)
    {
        return new TwoFieldParam(fn1, fn2, " != ");
    }

    static <T, R> Param notEq(boolean condition, SFunction<T, ?> fn1, SFunction<R, ?> fn2)
    {
        if (condition)
        {
            return new TwoFieldParam(fn1, fn2, " != ");
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param bt(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, ">");
    }

    static <T> Param bt(boolean condition, SFunction<T, ?> fn, Object value)
    {
        if (condition)
        {
            return new OneValueParam(fn, value, ">");
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param lt(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, "<");
    }

    static <T> Param lt(boolean condition, SFunction<T, ?> fn, Object value)
    {
        if (condition)
        {
            return new OneValueParam(fn, value, "<");
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param be(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, ">=");
    }

    static <T> Param be(boolean condition, SFunction<T, ?> fn, Object value)
    {
        if (condition)
        {
            return new OneValueParam(fn, value, ">=");
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param le(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, "<=");
    }

    static <T> Param le(boolean condition, SFunction<T, ?> fn, Object value)
    {
        if (condition)
        {
            return new OneValueParam(fn, value, "<=");
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param between(SFunction<T, ?> fn, Object value1, Object value2)
    {
        return new BetweenParam(fn, value1, value2);
    }

    static <T> Param between(boolean condition, SFunction<T, ?> fn, Object value1, Object value2)
    {
        if (condition)
        {
            return new BetweenParam(fn, value1, value2);
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param startWith(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.AFTER, value, false);
    }

    static <T> Param startWith(boolean condition, SFunction<T, ?> fn, Object value)
    {
        if (condition)
        {
            return new StringPatternParam(fn, StringPatternParam.PatternMode.AFTER, value, false);
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param endWith(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.BEFORE, value, false);
    }

    static <T> Param endWith(boolean condition, SFunction<T, ?> fn, Object value)
    {
        if (condition)
        {
            return new StringPatternParam(fn, StringPatternParam.PatternMode.BEFORE, value, false);
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param contain(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.BOTH, value, false);
    }

    static <T> Param contain(boolean condition, SFunction<T, ?> fn, Object value)
    {
        if (condition)
        {
            return new StringPatternParam(fn, StringPatternParam.PatternMode.BOTH, value, false);
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param like(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.NONE, value, false);
    }

    static <T> Param like(boolean condition, SFunction<T, ?> fn, Object value)
    {
        if (condition)
        {
            return new StringPatternParam(fn, StringPatternParam.PatternMode.NONE, value, false);
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param notStartWith(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.AFTER, value, true);
    }

    static <T> Param notStartWith(boolean condition, SFunction<T, ?> fn, Object value)
    {
        if (condition)
        {
            return new StringPatternParam(fn, StringPatternParam.PatternMode.AFTER, value, true);
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param notEndWith(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.BEFORE, value, true);
    }

    static <T> Param notEndWith(boolean condition, SFunction<T, ?> fn, Object value)
    {
        if (condition)
        {
            return new StringPatternParam(fn, StringPatternParam.PatternMode.BEFORE, value, true);
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param notContain(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.BOTH, value, true);
    }

    static <T> Param notContain(boolean condition, SFunction<T, ?> fn, Object value)
    {
        if (condition)
        {
            return new StringPatternParam(fn, StringPatternParam.PatternMode.BOTH, value, true);
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param in(SFunction<T, ?> fn, int... values)
    {
        return new InParam(fn, InParam.IN, Arrays.asList(values));
    }

    static <T> Param in(boolean condition, SFunction<T, ?> fn, int... values)
    {
        if (condition)
        {
            return new InParam(fn, InParam.IN, Arrays.asList(values));
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param in(SFunction<T, ?> fn, long... values)
    {
        return new InParam(fn, InParam.IN, Arrays.asList(values));
    }

    static <T> Param in(boolean condition, SFunction<T, ?> fn, long... values)
    {
        if (condition)
        {
            return new InParam(fn, InParam.IN, Arrays.asList(values));
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param in(SFunction<T, ?> fn, float... values)
    {
        return new InParam(fn, InParam.IN, Arrays.asList(values));
    }

    static <T> Param in(boolean condition, SFunction<T, ?> fn, float... values)
    {
        if (condition)
        {
            return new InParam(fn, InParam.IN, Arrays.asList(values));
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param in(SFunction<T, ?> fn, double... values)
    {
        return new InParam(fn, InParam.IN, Arrays.asList(values));
    }

    static <T> Param in(boolean condition, SFunction<T, ?> fn, double... values)
    {
        if (condition)
        {
            return new InParam(fn, InParam.IN, Arrays.asList(values));
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param in(SFunction<T, ?> fn, String... values)
    {
        return new InParam(fn, InParam.IN, Arrays.asList(values));
    }

    static <T> Param in(boolean condition, SFunction<T, ?> fn, String... values)
    {
        if (condition)
        {
            return new InParam(fn, InParam.IN, Arrays.asList(values));
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param in(SFunction<T, ?> fn, Collection<?> values)
    {
        return new InParam(fn, InParam.IN, values);
    }

    static <T> Param in(boolean condition, SFunction<T, ?> fn, Collection<?> values)
    {
        if (condition)
        {
            return new InParam(fn, InParam.IN, values);
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param notIn(SFunction<T, ?> fn, Collection<?> values)
    {
        return new InParam(fn, InParam.NOT_IN, values);
    }

    static <T> Param notIn(boolean condition, SFunction<T, ?> fn, Collection<?> values)
    {
        if (condition)
        {
            return new InParam(fn, InParam.NOT_IN, values);
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param notIn(SFunction<T, ?> fn, int... values)
    {
        return new InParam(fn, InParam.NOT_IN, Arrays.asList(values));
    }

    static <T> Param notIn(boolean condition, SFunction<T, ?> fn, int... values)
    {
        if (condition)
        {
            return new InParam(fn, InParam.NOT_IN, Arrays.asList(values));
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param notIn(SFunction<T, ?> fn, long... values)
    {
        return new InParam(fn, InParam.NOT_IN, Arrays.asList(values));
    }

    static <T> Param notIn(boolean condition, SFunction<T, ?> fn, long... values)
    {
        if (condition)
        {
            return new InParam(fn, InParam.NOT_IN, Arrays.asList(values));
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param notIn(SFunction<T, ?> fn, double... values)
    {
        return new InParam(fn, InParam.NOT_IN, Arrays.asList(values));
    }

    static <T> Param notIn(boolean condition, SFunction<T, ?> fn, double... values)
    {
        if (condition)
        {
            return new InParam(fn, InParam.NOT_IN, Arrays.asList(values));
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param notIn(SFunction<T, ?> fn, float... values)
    {
        return new InParam(fn, InParam.NOT_IN, Arrays.asList(values));
    }

    static <T> Param notIn(boolean condition, SFunction<T, ?> fn, float... values)
    {
        if (condition)
        {
            return new InParam(fn, InParam.NOT_IN, Arrays.asList(values));
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }

    static <T> Param notIn(SFunction<T, ?> fn, String... values)
    {
        return new InParam(fn, InParam.NOT_IN, Arrays.asList(values));
    }

    static <T> Param notIn(boolean condition, SFunction<T, ?> fn, String... values)
    {
        if (condition)
        {
            return new InParam(fn, InParam.NOT_IN, Arrays.asList(values));
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }
}
