package com.jfireframework.sql.dao;

import java.sql.Connection;
import java.util.List;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;

public interface StrategyOperation<T>
{
    public int update(Connection connection, T param, String strategy);
    
    public T findOne(Connection connection, T entity, String strategy);
    
    public List<T> findAll(Connection connection, T param, String strategy);
    
    public List<T> findPage(Connection connection, T param, Page page, PageParse pageParse, String strategy);
}
