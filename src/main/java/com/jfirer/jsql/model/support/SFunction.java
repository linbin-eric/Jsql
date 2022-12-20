package com.jfirer.jsql.model.support;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable
{
    //这个方法返回的SerializedLambda是重点
    static <T> SerializedLambda getSerializedLambda(SFunction<T, ?> fn) throws Exception
    {
        //writeReplace改了好像会报异常
        Method write = fn.getClass().getDeclaredMethod("writeReplace");
        write.setAccessible(true);
        return (SerializedLambda) write.invoke(fn);
    }

    static String getImplClass(SFunction fn)
    {
        try
        {
            String resourceName = getSerializedLambda(fn).getImplClass();
            return resourceName.replace("/", ".");
        }
        catch (Exception e)
        {
            return null;
        }
    }

    static <T> String getImplMethodName(SFunction<T, ?> fn)
    {
        try
        {
            return getSerializedLambda(fn).getImplMethodName();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    default String resolveFieldName()
    {
        return resolveFieldName(this);
    }

    default String getImplClass()
    {
        return getImplClass(this);
    }

    static <T> String resolveFieldName(SFunction<T, ?> fn)
    {
        String methodName = getImplMethodName(fn);
        if (methodName.startsWith("get") || methodName.startsWith("is"))
        {
            return Optional.of(methodName.startsWith("get") ? methodName.substring(3) : methodName.substring(2))//
                           .map(value -> value.toLowerCase().charAt(0) + value.substring(1))//
                           .get();
        }
        else
        {
            return methodName;
        }
    }
}
