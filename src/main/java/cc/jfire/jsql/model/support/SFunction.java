package cc.jfire.jsql.model.support;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable
{
    ConcurrentMap<SFunction, Class<?>> implClassNameMap = new ConcurrentHashMap<>();
    ConcurrentMap<SFunction, String>   fieldNameMap     = new ConcurrentHashMap<>();

    //这个方法返回的SerializedLambda是重点
    static SerializedLambda getSerializedLambda(SFunction<?, ?> fn) throws Exception
    {
        //writeReplace改了好像会报异常
        Method write = fn.getClass().getDeclaredMethod("writeReplace");
        write.setAccessible(true);
        return (SerializedLambda) write.invoke(fn);
    }

    static Class<?> getImplClass(SFunction fn)
    {
        return implClassNameMap.computeIfAbsent(fn, sfn -> {
            try
            {
                String resourceName = getSerializedLambda(sfn).getImplClass();
                return Thread.currentThread().getContextClassLoader().loadClass(resourceName.replace("/", "."));
            }
            catch (Exception e)
            {
                return null;
            }
        });
    }

    static String getImplMethodName(SFunction<?, ?> fn)
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

    default Class<?> getImplClass()
    {
        return getImplClass(this);
    }

    static String resolveFieldName(SFunction<?, ?> fn)
    {
        return fieldNameMap.computeIfAbsent(fn, sfn -> {
            String methodName = getImplMethodName(sfn);
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
        });
    }
}
