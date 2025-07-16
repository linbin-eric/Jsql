package com.jfirer.jsql.model.param;

import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.support.SFunction;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class InParam extends InternalParamImpl
{
    public static final String IN     = " in (";
    public static final String NOT_IN = " not in (";

    public InParam(SFunction<?, ?> fn, String mode, Collection<Object> values)
    {
        super(fn);
        if (values.isEmpty())
        {
            throw new IllegalArgumentException("集合参数不能为空");
        }
        consumer = (columnName, builder, paramValues) -> {
            builder.append(columnName).append(mode);
            record WrapperData(String segment, Object paramValue)
            {
            }
            if (values.size() > 1)
            {
                String segment = values.stream()//
                                       .map(value -> {
                                           if (value instanceof Model m)
                                           {
                                               Model.ModelResult result = m.getResult();
                                               return new WrapperData("(" + result.sql() + ")", result.paramValues());
                                           }
                                           else
                                           {
                                               return new WrapperData("?", value);
                                           }
                                       })//
                                       .peek(data -> {
                                           if (data.paramValue instanceof List<?> l)
                                           {
                                               paramValues.addAll(l);
                                           }
                                           else
                                           {
                                               paramValues.add(data.paramValue);
                                           }
                                       }).map(data -> data.segment).collect(Collectors.joining(","));
                builder.append(segment).append(" )");
            }
            else
            {
                Object value = values.iterator().next();
                if (value instanceof int[] array)
                {
                    builder.append("?");
                    paramValues.add(array[0]);
                    for (int i = 1; i < array.length; i++)
                    {
                        builder.append(",?");
                        paramValues.add(array[i]);
                    }
                }
                else if (value instanceof long[] array)
                {
                    builder.append("?");
                    paramValues.add(array[0]);
                    for (int i = 1; i < array.length; i++)
                    {
                        builder.append(",?");
                        paramValues.add(array[i]);
                    }
                }
                else if (value instanceof Integer[] array)
                {
                    builder.append("?");
                    paramValues.add(array[0]);
                    for (int i = 1; i < array.length; i++)
                    {
                        builder.append(",?");
                        paramValues.add(array[i]);
                    }
                }
                else if (value instanceof Long[] array)
                {
                    builder.append("?");
                    paramValues.add(array[0]);
                    for (int i = 1; i < array.length; i++)
                    {
                        builder.append(",?");
                        paramValues.add(array[i]);
                    }
                }
                else if (value instanceof String[] array)
                {
                    builder.append("?");
                    paramValues.add(array[0]);
                    for (int i = 1; i < array.length; i++)
                    {
                        builder.append(",?");
                        paramValues.add(array[i]);
                    }
                }
                else if (value instanceof Integer i)
                {
                    builder.append("?");
                    paramValues.add(i);
                }
                else if (value instanceof Long l)
                {
                    builder.append("?");
                    paramValues.add(l);
                }
                else if (value instanceof String s)
                {
                    builder.append("?");
                    paramValues.add(s);
                }
                else if (value instanceof Double d)
                {
                    builder.append("?");
                    paramValues.add(d);
                }
                else if (value instanceof Float f)
                {
                    builder.append("?");
                    paramValues.add(f);
                }
                else if (value instanceof Boolean b)
                {
                    builder.append("?");
                    paramValues.add(b);
                }
                else
                {
                    throw new IllegalArgumentException("无法识别的类型:" + value.getClass().getName());
                }
                builder.append(" )");
            }
        };
    }
}
