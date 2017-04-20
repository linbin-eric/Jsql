package com.jfireframework.sql.session.impl;

import java.sql.SQLException;
import java.util.IdentityHashMap;
import javax.sql.DataSource;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.dao.Dao;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.session.SessionFactory;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.session.mapper.Mapper;

public class SessionFactoryImpl implements SessionFactory
{
    private final IdentityHashMap<Class<?>, Mapper> mappers;
    private final IdentityHashMap<Class<?>, Dao<?>> daos;
    private final SqlInterceptor[]                  sqlInterceptors;
    private final PageParse                         pageParse;
    private final DataSource                        dataSource;
    
    public SessionFactoryImpl(IdentityHashMap<Class<?>, Mapper> mappers, IdentityHashMap<Class<?>, Dao<?>> daos, SqlInterceptor[] sqlInterceptors, PageParse pageParse, DataSource dataSource)
    {
        this.mappers = mappers;
        this.daos = daos;
        this.sqlInterceptors = sqlInterceptors;
        this.pageParse = pageParse;
        this.dataSource = dataSource;
        for (Mapper each : mappers.values())
        {
            each.setSessionFactory(this);
        }
    }
    
    @Override
    public SqlSession getCurrentSession()
    {
        return CURRENT_SESSION.get();
    }
    
    @Override
    public SqlSession openSession()
    {
        try
        {
            SqlSession session = new SqlSessionImpl(dataSource.getConnection(), this, sqlInterceptors, pageParse);
            return session;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public SqlSession getOrCreateCurrentSession()
    {
        SqlSession session = getCurrentSession();
        if (session == null)
        {
            session = openSession();
        }
        return session;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getMapper(Class<T> entityClass)
    {
        try
        {
            return (T) mappers.get(entityClass);
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> Dao<T> getDao(Class<T> ckass)
    {
        return (Dao<T>) daos.get(ckass);
    }
    
    @Override
    public void cleanAllData()
    {
        SqlSession session = getOrCreateCurrentSession();
        session.beginTransAction(0);
        for (Dao<?> dao : daos.values())
        {
            dao.deleteAll(session);
        }
        session.commit();
        session.close();
    }
    
}
