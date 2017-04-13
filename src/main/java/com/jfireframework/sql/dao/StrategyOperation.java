package com.jfireframework.sql.dao;

import java.util.List;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.session.SqlSession;

public interface StrategyOperation<T>
{
    public int update(SqlSession session, T param, String strategy);
    
    public T findOne(SqlSession session, T entity, String strategy);
    
    public List<T> findAll(SqlSession session, T param, String strategy);
    
    public List<T> findPage(SqlSession session, T param, Page page, PageParse pageParse, String strategy);
}
