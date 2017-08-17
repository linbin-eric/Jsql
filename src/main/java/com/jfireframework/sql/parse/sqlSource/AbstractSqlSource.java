package com.jfireframework.sql.parse.sqlSource;

import java.lang.reflect.Method;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;

public abstract class AbstractSqlSource implements SqlSource
{
    protected String buildParam(String inject, String[] paramNames, Class<?>[] paramTypes)
    {
        boolean before = false;
        boolean after = false;
        if (inject.startsWith("%"))
        {
            inject = inject.substring(1);
            before = true;
        }
        if (inject.endsWith("%"))
        {
            inject = inject.substring(0, inject.length() - 1);
            after = true;
        }
        String result = "";
        if (before)
        {
            result += "\"%\"+";
        }
        result += buildInvoke(inject, paramNames, paramTypes);
        if (after)
        {
            result += "+\"%\"";
        }
        return result;
    }
    
    /**
     * 根据表达式生成在java语言下的调用方式。<br/>
     * 比如user.age就会被转化为user.getAge(),或者user.boy 被转化为user.isBoy();<br/>
     * 如果遇到方法调用的，也可以识别。比如home.getUser().name就可以被转化为home.getUser().getName()
     * 
     * @param content
     * @param paramNames
     * @param types
     * @return
     */
    protected String buildInvoke(String content, String[] paramNames, Class<?>[] types)
    {
        StringCache cache = new StringCache();
        if (content.indexOf('.') == -1)
        {
            int i = 0;
            for (String each : paramNames)
            {
                if (each.equals(content))
                {
                    break;
                }
                else
                {
                    i++;
                }
            }
            cache.append("$").append(i);
        }
        else
        {
            String[] tmp = content.split("\\.");
            content = tmp[0];
            int i = 0;
            for (String each : paramNames)
            {
                if (each.equals(content))
                {
                    break;
                }
                else
                {
                    i++;
                }
            }
            cache.append("$").append(i);
            String invokeName;
            Class<?> type = types[i];
            int index = 1;
            Method method;
            while (index < tmp.length)
            {
                String name = tmp[index];
                try
                {
                    if (name.endsWith("()"))
                    {
                        String methodName = name.substring(0, name.length() - 2);
                        method = type.getMethod(methodName);
                    }
                    else
                    {
                        method = type.getMethod("get" + Character.toUpperCase(tmp[index].charAt(0)) + tmp[index].substring(1));
                    }
                }
                catch (Exception e)
                {
                    try
                    {
                        method = types[i].getMethod("is" + Character.toUpperCase(tmp[index].charAt(0)) + tmp[index].substring(1));
                    }
                    catch (Exception e1)
                    {
                        throw new JustThrowException(e1);
                    }
                }
                invokeName = method.getName() + "()";
                cache.append(".").append(invokeName);
                type = method.getReturnType();
                index += 1;
            }
            
        }
        return cache.toString();
    }
    
}
