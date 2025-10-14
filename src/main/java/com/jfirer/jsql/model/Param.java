package com.jfirer.jsql.model;

import com.jfirer.jsql.model.param.*;
import com.jfirer.jsql.model.support.SFunction;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Param接口用于构建SQL查询的条件表达式。
 * <p>
 * 该接口提供了丰富的静态工厂方法来创建各种类型的查询条件，包括比较运算、逻辑运算、
 * 字符串匹配、IN查询、位运算等。所有的静态方法都提供了两个版本：
 * </p>
 * <ul>
 *   <li><b>基础版本</b>：直接构建条件，例如：{@code Param.eq(User::getName, "张三")}</li>
 *   <li><b>条件版本</b>：接受一个boolean参数，当条件为false时返回{@link NoOpParam}，不会添加任何条件，
 *       例如：{@code Param.eq(name != null, User::getName, name)}</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 基础条件构建
 * Param param1 = Param.eq(User::getName, "张三");
 *
 * // 条件组合
 * Param param2 = Param.eq(User::getName, "张三")
 *                     .and(Param.bt(User::getAge, 18));
 *
 * // 条件参数版本，避免null值导致的SQL错误
 * String name = getUserInput(); // 可能为null
 * Param param3 = Param.eq(name != null, User::getName, name)
 *                     .and(Param.bt(User::getAge, 18));
 *
 * // 字符串匹配
 * Param param4 = Param.startWith(User::getName, "张")
 *                     .or(Param.contain(User::getEmail, "@gmail"));
 *
 * // IN查询
 * Param param5 = Param.in(User::getId, 1, 2, 3, 4, 5);
 * Param param6 = Param.in(User::getId, Arrays.asList(1, 2, 3));
 *
 * // 在Model API中使用
 * List<User> users = Model.select(User.class)
 *                         .where(Param.eq(User::getStatus, 1)
 *                                     .and(Param.bt(User::getAge, 18)))
 *                         .list(session);
 * }</pre>
 *
 * <h3>条件参数的优势：</h3>
 * <p>
 * 使用带boolean条件的方法可以简化动态查询的构建，避免大量的if-else判断：
 * </p>
 * <pre>{@code
 * // 传统方式（不推荐）
 * Param param = null;
 * if (name != null) {
 *     param = Param.eq(User::getName, name);
 * }
 * if (age != null) {
 *     if (param == null) {
 *         param = Param.bt(User::getAge, age);
 *     } else {
 *         param = param.and(Param.bt(User::getAge, age));
 *     }
 * }
 *
 * // 使用条件参数（推荐）
 * Param param = Param.eq(name != null, User::getName, name)
 *                    .and(Param.bt(age != null, User::getAge, age));
 * }</pre>
 *
 * @see NoOpParam
 * @see SFunction
 */
public interface Param
{
    /**
     * 使用AND逻辑连接当前条件和另一个条件。
     *
     * @param param 要连接的条件参数
     * @return 连接后的条件
     */
    Param and(Param param);

    /**
     * 根据条件决定是否使用AND逻辑连接另一个条件。
     * <p>
     * 当supplier返回true时，执行AND连接；否则返回当前条件不变。
     * </p>
     *
     * @param supplier 条件供应器
     * @param param 要连接的条件参数
     * @return 连接后的条件或当前条件
     */
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

    /**
     * 使用OR逻辑连接当前条件和另一个条件。
     *
     * @param param 要连接的条件参数
     * @return 连接后的条件
     */
    Param or(Param param);

    /**
     * 根据条件决定是否使用OR逻辑连接另一个条件。
     * <p>
     * 当supplier返回true时，执行OR连接；否则返回当前条件不变。
     * </p>
     *
     * @param supplier 条件供应器
     * @param param 要连接的条件参数
     * @return 连接后的条件或当前条件
     */
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

    /**
     * 将当前条件用括号包裹，形成一个独立的条件组。
     * <p>
     * 例如：(condition1 OR condition2) AND condition3
     * </p>
     *
     * @return 包裹后的条件
     */
    Param union();

    /**
     * 创建一个IS NULL条件。
     *
     * @param fn 实体属性的方法引用
     * @param <T> 实体类型
     * @return IS NULL条件
     */
    static <T> Param isNull(SFunction<T, ?> fn)
    {
        return new NullValueParam(fn);
    }

    /**
     * 根据条件创建IS NULL条件。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param <T> 实体类型
     * @return IS NULL条件或NoOpParam
     */
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

    /**
     * 创建一个IS NOT NULL条件。
     *
     * @param fn 实体属性的方法引用
     * @param <T> 实体类型
     * @return IS NOT NULL条件
     */
    static <T> Param notNull(SFunction<T, ?> fn)
    {
        return new NotNullValueParam(fn);
    }

    /**
     * 根据条件创建IS NOT NULL条件。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param <T> 实体类型
     * @return IS NOT NULL条件或NoOpParam
     */
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

    /**
     * 创建一个等于（=）条件，用于字段与值的比较。
     *
     * @param fn 实体属性的方法引用
     * @param value 比较值
     * @param <T> 实体类型
     * @return 等于条件
     */
    static <T> Param eq(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, "=");
    }

    /**
     * 根据条件创建等于（=）条件，用于字段与值的比较。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param value 比较值
     * @param <T> 实体类型
     * @return 等于条件或NoOpParam
     */
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

    /**
     * 创建一个等于（=）条件，用于两个字段之间的比较。
     *
     * @param fn1 第一个实体属性的方法引用
     * @param fn2 第二个实体属性的方法引用
     * @param <T> 第一个实体类型
     * @param <R> 第二个实体类型
     * @return 等于条件
     */
    static <T, R> Param eq(SFunction<T, ?> fn1, SFunction<R, ?> fn2)
    {
        return new TwoFieldParam(fn1, fn2, " = ");
    }

    /**
     * 根据条件创建等于（=）条件，用于两个字段之间的比较。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn1 第一个实体属性的方法引用
     * @param fn2 第二个实体属性的方法引用
     * @param <T> 第一个实体类型
     * @param <R> 第二个实体类型
     * @return 等于条件或NoOpParam
     */
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

    /**
     * 创建一个不等于（!=）条件，用于字段与值的比较。
     *
     * @param fn 实体属性的方法引用
     * @param value 比较值
     * @param <T> 实体类型
     * @return 不等于条件
     */
    static <T> Param notEq(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, "!=");
    }

    /**
     * 根据条件创建不等于（!=）条件，用于字段与值的比较。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param value 比较值
     * @param <T> 实体类型
     * @return 不等于条件或NoOpParam
     */
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

    /**
     * 创建一个不等于（!=）条件，用于两个字段之间的比较。
     *
     * @param fn1 第一个实体属性的方法引用
     * @param fn2 第二个实体属性的方法引用
     * @param <T> 第一个实体类型
     * @param <R> 第二个实体类型
     * @return 不等于条件
     */
    static <T, R> Param notEq(SFunction<T, ?> fn1, SFunction<R, ?> fn2)
    {
        return new TwoFieldParam(fn1, fn2, " != ");
    }

    /**
     * 根据条件创建不等于（!=）条件，用于两个字段之间的比较。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn1 第一个实体属性的方法引用
     * @param fn2 第二个实体属性的方法引用
     * @param <T> 第一个实体类型
     * @param <R> 第二个实体类型
     * @return 不等于条件或NoOpParam
     */
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

    /**
     * 创建一个大于（&gt;）条件。
     *
     * @param fn 实体属性的方法引用
     * @param value 比较值
     * @param <T> 实体类型
     * @return 大于条件
     */
    static <T> Param bt(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, ">");
    }

    /**
     * 根据条件创建大于（&gt;）条件。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param value 比较值
     * @param <T> 实体类型
     * @return 大于条件或NoOpParam
     */
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

    /**
     * 创建一个小于（&lt;）条件。
     *
     * @param fn 实体属性的方法引用
     * @param value 比较值
     * @param <T> 实体类型
     * @return 小于条件
     */
    static <T> Param lt(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, "<");
    }

    /**
     * 根据条件创建小于（&lt;）条件。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param value 比较值
     * @param <T> 实体类型
     * @return 小于条件或NoOpParam
     */
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

    /**
     * 创建一个大于等于（&gt;=）条件。
     *
     * @param fn 实体属性的方法引用
     * @param value 比较值
     * @param <T> 实体类型
     * @return 大于等于条件
     */
    static <T> Param be(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, ">=");
    }

    /**
     * 根据条件创建大于等于（&gt;=）条件。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param value 比较值
     * @param <T> 实体类型
     * @return 大于等于条件或NoOpParam
     */
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

    /**
     * 创建一个小于等于（&lt;=）条件。
     *
     * @param fn 实体属性的方法引用
     * @param value 比较值
     * @param <T> 实体类型
     * @return 小于等于条件
     */
    static <T> Param le(SFunction<T, ?> fn, Object value)
    {
        return new OneValueParam(fn, value, "<=");
    }

    /**
     * 根据条件创建小于等于（&lt;=）条件。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param value 比较值
     * @param <T> 实体类型
     * @return 小于等于条件或NoOpParam
     */
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

    /**
     * 创建一个BETWEEN条件，用于范围查询。
     *
     * @param fn 实体属性的方法引用
     * @param value1 范围起始值
     * @param value2 范围结束值
     * @param <T> 实体类型
     * @return BETWEEN条件
     */
    static <T> Param between(SFunction<T, ?> fn, Object value1, Object value2)
    {
        return new BetweenParam(fn, value1, value2);
    }

    /**
     * 根据条件创建BETWEEN条件，用于范围查询。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param value1 范围起始值
     * @param value2 范围结束值
     * @param <T> 实体类型
     * @return BETWEEN条件或NoOpParam
     */
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

    /**
     * 创建一个字符串以指定值开头的LIKE条件（value%）。
     *
     * @param fn 实体属性的方法引用
     * @param value 匹配值
     * @param <T> 实体类型
     * @return 字符串开头匹配条件
     */
    static <T> Param startWith(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.AFTER, value, false);
    }

    /**
     * 根据条件创建字符串以指定值开头的LIKE条件（value%）。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param value 匹配值
     * @param <T> 实体类型
     * @return 字符串开头匹配条件或NoOpParam
     */
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

    /**
     * 创建一个字符串以指定值结尾的LIKE条件（%value）。
     *
     * @param fn 实体属性的方法引用
     * @param value 匹配值
     * @param <T> 实体类型
     * @return 字符串结尾匹配条件
     */
    static <T> Param endWith(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.BEFORE, value, false);
    }

    /**
     * 根据条件创建字符串以指定值结尾的LIKE条件（%value）。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param value 匹配值
     * @param <T> 实体类型
     * @return 字符串结尾匹配条件或NoOpParam
     */
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

    /**
     * 创建一个字符串包含指定值的LIKE条件（%value%）。
     *
     * @param fn 实体属性的方法引用
     * @param value 匹配值
     * @param <T> 实体类型
     * @return 字符串包含匹配条件
     */
    static <T> Param contain(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.BOTH, value, false);
    }

    /**
     * 根据条件创建字符串包含指定值的LIKE条件（%value%）。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param value 匹配值
     * @param <T> 实体类型
     * @return 字符串包含匹配条件或NoOpParam
     */
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

    /**
     * 创建一个自定义LIKE条件，value中可以包含%和_通配符。
     *
     * @param fn 实体属性的方法引用
     * @param value 匹配值，可包含SQL通配符
     * @param <T> 实体类型
     * @return 自定义LIKE条件
     */
    static <T> Param like(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.NONE, value, false);
    }

    /**
     * 根据条件创建自定义LIKE条件，value中可以包含%和_通配符。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param value 匹配值，可包含SQL通配符
     * @param <T> 实体类型
     * @return 自定义LIKE条件或NoOpParam
     */
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

    /**
     * 创建一个字符串不以指定值开头的NOT LIKE条件。
     *
     * @param fn 实体属性的方法引用
     * @param value 匹配值
     * @param <T> 实体类型
     * @return 字符串不以开头匹配条件
     */
    static <T> Param notStartWith(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.AFTER, value, true);
    }

    /**
     * 根据条件创建字符串不以指定值开头的NOT LIKE条件。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param value 匹配值
     * @param <T> 实体类型
     * @return 字符串不以开头匹配条件或NoOpParam
     */
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

    /**
     * 创建一个字符串不以指定值结尾的NOT LIKE条件。
     *
     * @param fn 实体属性的方法引用
     * @param value 匹配值
     * @param <T> 实体类型
     * @return 字符串不以结尾匹配条件
     */
    static <T> Param notEndWith(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.BEFORE, value, true);
    }

    /**
     * 根据条件创建字符串不以指定值结尾的NOT LIKE条件。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param value 匹配值
     * @param <T> 实体类型
     * @return 字符串不以结尾匹配条件或NoOpParam
     */
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

    /**
     * 创建一个字符串不包含指定值的NOT LIKE条件。
     *
     * @param fn 实体属性的方法引用
     * @param value 匹配值
     * @param <T> 实体类型
     * @return 字符串不包含匹配条件
     */
    static <T> Param notContain(SFunction<T, ?> fn, Object value)
    {
        return new StringPatternParam(fn, StringPatternParam.PatternMode.BOTH, value, true);
    }

    /**
     * 根据条件创建字符串不包含指定值的NOT LIKE条件。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param value 匹配值
     * @param <T> 实体类型
     * @return 字符串不包含匹配条件或NoOpParam
     */
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

    /**
     * 创建一个IN条件，用于int数组。
     *
     * @param fn 实体属性的方法引用
     * @param values int类型的值数组
     * @param <T> 实体类型
     * @return IN条件
     */
    static <T> Param in(SFunction<T, ?> fn, int... values)
    {
        return new InParam(fn, InParam.IN, Arrays.asList(values));
    }

    /**
     * 根据条件创建IN条件，用于int数组。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param values int类型的值数组
     * @param <T> 实体类型
     * @return IN条件或NoOpParam
     */
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

    /**
     * 创建一个IN条件，用于long数组。
     *
     * @param fn 实体属性的方法引用
     * @param values long类型的值数组
     * @param <T> 实体类型
     * @return IN条件
     */
    static <T> Param in(SFunction<T, ?> fn, long... values)
    {
        return new InParam(fn, InParam.IN, Arrays.asList(values));
    }

    /**
     * 根据条件创建IN条件，用于long数组。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param values long类型的值数组
     * @param <T> 实体类型
     * @return IN条件或NoOpParam
     */
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

    /**
     * 创建一个IN条件，用于float数组。
     *
     * @param fn 实体属性的方法引用
     * @param values float类型的值数组
     * @param <T> 实体类型
     * @return IN条件
     */
    static <T> Param in(SFunction<T, ?> fn, float... values)
    {
        return new InParam(fn, InParam.IN, Arrays.asList(values));
    }

    /**
     * 根据条件创建IN条件，用于float数组。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param values float类型的值数组
     * @param <T> 实体类型
     * @return IN条件或NoOpParam
     */
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

    /**
     * 创建一个IN条件，用于double数组。
     *
     * @param fn 实体属性的方法引用
     * @param values double类型的值数组
     * @param <T> 实体类型
     * @return IN条件
     */
    static <T> Param in(SFunction<T, ?> fn, double... values)
    {
        return new InParam(fn, InParam.IN, Arrays.asList(values));
    }

    /**
     * 根据条件创建IN条件，用于double数组。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param values double类型的值数组
     * @param <T> 实体类型
     * @return IN条件或NoOpParam
     */
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

    /**
     * 创建一个IN条件，用于String数组。
     *
     * @param fn 实体属性的方法引用
     * @param values String类型的值数组
     * @param <T> 实体类型
     * @return IN条件
     */
    static <T> Param in(SFunction<T, ?> fn, String... values)
    {
        return new InParam(fn, InParam.IN, Arrays.asList(values));
    }

    /**
     * 根据条件创建IN条件，用于String数组。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param values String类型的值数组
     * @param <T> 实体类型
     * @return IN条件或NoOpParam
     */
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

    /**
     * 创建一个IN条件，用于集合。
     * <p>
     * 这是最通用的IN条件方法，可以接受任意类型的集合。
     * </p>
     *
     * @param fn 实体属性的方法引用
     * @param values 值的集合
     * @param <T> 实体类型
     * @return IN条件
     */
    static <T> Param in(SFunction<T, ?> fn, Collection<?> values)
    {
        return new InParam(fn, InParam.IN, values);
    }

    /**
     * 根据条件创建IN条件，用于集合。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param values 值的集合
     * @param <T> 实体类型
     * @return IN条件或NoOpParam
     */
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

    /**
     * 创建一个NOT IN条件，用于集合。
     *
     * @param fn 实体属性的方法引用
     * @param values 值的集合
     * @param <T> 实体类型
     * @return NOT IN条件
     */
    static <T> Param notIn(SFunction<T, ?> fn, Collection<?> values)
    {
        return new InParam(fn, InParam.NOT_IN, values);
    }

    /**
     * 根据条件创建NOT IN条件，用于集合。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param values 值的集合
     * @param <T> 实体类型
     * @return NOT IN条件或NoOpParam
     */
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

    /**
     * 创建一个NOT IN条件，用于int数组。
     *
     * @param fn 实体属性的方法引用
     * @param values int类型的值数组
     * @param <T> 实体类型
     * @return NOT IN条件
     */
    static <T> Param notIn(SFunction<T, ?> fn, int... values)
    {
        return new InParam(fn, InParam.NOT_IN, Arrays.asList(values));
    }

    /**
     * 根据条件创建NOT IN条件，用于int数组。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param values int类型的值数组
     * @param <T> 实体类型
     * @return NOT IN条件或NoOpParam
     */
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

    /**
     * 创建一个NOT IN条件，用于long数组。
     *
     * @param fn 实体属性的方法引用
     * @param values long类型的值数组
     * @param <T> 实体类型
     * @return NOT IN条件
     */
    static <T> Param notIn(SFunction<T, ?> fn, long... values)
    {
        return new InParam(fn, InParam.NOT_IN, Arrays.asList(values));
    }

    /**
     * 根据条件创建NOT IN条件，用于long数组。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param values long类型的值数组
     * @param <T> 实体类型
     * @return NOT IN条件或NoOpParam
     */
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

    /**
     * 创建一个NOT IN条件，用于double数组。
     *
     * @param fn 实体属性的方法引用
     * @param values double类型的值数组
     * @param <T> 实体类型
     * @return NOT IN条件
     */
    static <T> Param notIn(SFunction<T, ?> fn, double... values)
    {
        return new InParam(fn, InParam.NOT_IN, Arrays.asList(values));
    }

    /**
     * 根据条件创建NOT IN条件，用于double数组。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param values double类型的值数组
     * @param <T> 实体类型
     * @return NOT IN条件或NoOpParam
     */
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

    /**
     * 创建一个NOT IN条件，用于float数组。
     *
     * @param fn 实体属性的方法引用
     * @param values float类型的值数组
     * @param <T> 实体类型
     * @return NOT IN条件
     */
    static <T> Param notIn(SFunction<T, ?> fn, float... values)
    {
        return new InParam(fn, InParam.NOT_IN, Arrays.asList(values));
    }

    /**
     * 根据条件创建NOT IN条件，用于float数组。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param values float类型的值数组
     * @param <T> 实体类型
     * @return NOT IN条件或NoOpParam
     */
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

    /**
     * 创建一个NOT IN条件，用于String数组。
     *
     * @param fn 实体属性的方法引用
     * @param values String类型的值数组
     * @param <T> 实体类型
     * @return NOT IN条件
     */
    static <T> Param notIn(SFunction<T, ?> fn, String... values)
    {
        return new InParam(fn, InParam.NOT_IN, Arrays.asList(values));
    }

    /**
     * 根据条件创建NOT IN条件，用于String数组。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param values String类型的值数组
     * @param <T> 实体类型
     * @return NOT IN条件或NoOpParam
     */
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

    /**
     * 创建一个位运算条件，用于判断字段与指定位掩码进行AND运算后是否等于指定值。
     * <p>
     * 生成的SQL类似于: (field &amp; bitwise) = value
     * </p>
     * <p>
     * 常用于判断位标志，例如：
     * <ul>
     *   <li>判断某个标志位是否开启: {@code bitwiseAndByEquals(User::getFlags, 0x01, 0x01)}</li>
     *   <li>判断某个标志位是否关闭: {@code bitwiseAndByEquals(User::getFlags, 0x01, 0x00)}</li>
     * </ul>
     * </p>
     *
     * @param fn 实体属性的方法引用
     * @param bitwise 位掩码
     * @param value 期望的结果值
     * @param <T> 实体类型
     * @return 位运算条件
     */
    static <T> Param bitwiseAndByEquals(SFunction<T, ?> fn, int bitwise, int value)
    {
        return new BitwiseParam(fn, bitwise, "&", value, "=");
    }

    /**
     * 根据条件创建位运算条件，用于判断字段与指定位掩码进行AND运算后是否等于指定值。
     *
     * @param condition 是否创建条件，为false时返回NoOpParam
     * @param fn 实体属性的方法引用
     * @param bitwise 位掩码
     * @param value 期望的结果值
     * @param <T> 实体类型
     * @return 位运算条件或NoOpParam
     * @see #bitwiseAndByEquals(SFunction, int, int)
     */
    static <T> Param bitwiseAndByEquals(boolean condition, SFunction<T, ?> fn, int bitwise, int value)
    {
        if (condition)
        {
            return new BitwiseParam(fn, bitwise, "&", value, "=");
        }
        else
        {
            return NoOpParam.INSTANCE;
        }
    }
}
