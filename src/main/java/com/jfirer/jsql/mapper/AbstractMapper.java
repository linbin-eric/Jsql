package com.jfirer.jsql.mapper;

import com.jfirer.jsql.session.SqlSession;

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
    protected static final ThreadLocal<Map<String, Object>> cachedVariables = ThreadLocal.withInitial(() -> new HashMap<>());
    protected static final ThreadLocal<List<Object>>        cachedParams    = ThreadLocal.withInitial(() -> new ArrayList<>());
    protected              SqlSession                       session;

    public SqlSession getSession()
    {
        return session;
    }

    public void setSession(SqlSession session)
    {
        this.session = session;
    }
}
