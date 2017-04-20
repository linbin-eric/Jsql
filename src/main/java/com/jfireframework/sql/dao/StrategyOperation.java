package com.jfireframework.sql.dao;

import java.util.List;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.session.SqlSession;

public interface StrategyOperation<T>
{
    
    public int update(SqlSession session, String strategy, Object... params);
    
    public T findOne(SqlSession session, String strategy, Object... params);
    
    public List<T> findAll(SqlSession session, String strategy, Object... params);
    
    public List<T> findPage(SqlSession session, Page page, String strategy, Object... params);
}
