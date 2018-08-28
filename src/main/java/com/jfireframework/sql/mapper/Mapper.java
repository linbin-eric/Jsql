package com.jfireframework.sql.mapper;

import com.jfireframework.sql.session.SqlSession;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 用来给生成接口对象的类作为继承用 方便在其中设置sqlSession
 *
 * @author linbin
 */
public abstract class Mapper
{
    protected static final ThreadLocal<Map<String, Object>> cachedVariables = new ThreadLocal<Map<String, Object>>()
    {
        protected java.util.Map<String, Object> initialValue()
        {
            return new HashMap<String, Object>();
        }

        ;
    };
    protected static final ThreadLocal<List<Object>> cachedParams = new ThreadLocal<List<Object>>()
    {
        protected java.util.List<Object> initialValue()
        {
            return new LinkedList<Object>();
        }

        ;
    };

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
