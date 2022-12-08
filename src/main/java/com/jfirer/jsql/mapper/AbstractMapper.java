package com.jfirer.jsql.mapper;

import com.jfirer.jsql.session.SqlSession;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用来给生成接口对象的类作为继承用 方便在其中设置sqlSession
 *
 * @author linbin
 */
public abstract class AbstractMapper
{
    protected static final ThreadLocal<Map<String, Object>> cachedVariables = new ThreadLocal<Map<String, Object>>()
    {
        protected java.util.Map<String, Object> initialValue()
        {
            return new HashMap<String, Object>();
        }
    };
    protected static final ThreadLocal<List<Object>>        cachedParams    = new ThreadLocal<List<Object>>()
    {
        protected java.util.List<Object> initialValue()
        {
            return new ArrayList<Object>();
        }
    };
    public static          List<Method>                     methods         = new ArrayList<>();
    private static         int                              index           = 0;

    public static int put(Method method)
    {
        methods.add(index, method);
        index++;
        return index - 1;
    }

    protected Method getMethod(int index)
    {
        return methods.get(index);
    }

    protected SqlSession session;

    public SqlSession getSession()
    {
        return session;
    }

    public void setSession(SqlSession session)
    {
        this.session = session;
    }
}
