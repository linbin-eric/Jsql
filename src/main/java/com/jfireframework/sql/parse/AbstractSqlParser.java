package com.jfireframework.sql.parse;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.parse.lexer.Lexer;
import com.jfireframework.sql.parse.lexer.token.Expression;
import com.jfireframework.sql.parse.lexer.token.Token;

public class AbstractSqlParser implements SqlParse
{
    
    private String buildParam(String inject, String[] paramNames, Class<?>[] paramTypes)
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
    public String buildInvoke(String content, String[] paramNames, Class<?>[] types)
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
    
    @Override
    public String parseSingleQuery(String sql, MetaContext metaContext, Method method)
    {
        Lexer lexer = new Lexer(sql);
        lexer.parse(metaContext);
        if (lexer.isDynamic())
        {
            
        }
        else
        {
            String[] paramNames = method.getAnnotation(Sql.class).paramNames().split(",");
            Class<?>[] paramTypes = method.getParameterTypes();
            StringCache sqlCache = new StringCache();
            List<String> params = new ArrayList<String>();
            for (Token token : lexer.getTokens())
            {
                if (token.getTokenType() == Expression.VARIABLE)
                {
                    sqlCache.append('?').append(' ');
                    params.add(buildParam(token.getLiterals().substring(1), paramNames, paramTypes));
                }
                else if (token.getTokenType() == Expression.CONSTANT)
                {
                    sqlCache.append('?').append(' ');
                    params.add(token.getLiterals().substring(1));
                }
                else
                {
                    sqlCache.append(token.getLiterals()).append(' ');
                }
            }
        }
    }
    
    @Override
    public String parseListQuery(String sql, MetaContext metaContext, Method method)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String parsePageQuery(String sql, MetaContext metaContext, Method method)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String parseUpdate(String sql, MetaContext metaContext, Method method)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
