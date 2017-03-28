package com.jfireframework.sql.session.impl;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.dao.Dao;
import com.jfireframework.sql.session.SqlSession;

public class SessionFactoryImpl extends SessionFactoryBootstrap
{
    
    public SessionFactoryImpl()
    {
        
    }
    
    public SessionFactoryImpl(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }
    
    @Override
    public SqlSession getCurrentSession()
    {
        return sessionLocal.get();
    }
    
    @Override
    public SqlSession openSession()
    {
        try
        {
            SqlSession session = new SqlSessionImpl(dataSource.getConnection(), this, preInterceptors, sqlInterceptors, pageParse);
            return session;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void removeCurrentSession()
    {
        sessionLocal.remove();
    }
    
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }
    
    @Override
    public void setCurrentSession(SqlSession session)
    {
        sessionLocal.set(session);
    }
    
    @Override
    public void setScanPackage(String scanPackage)
    {
        this.scanPackage = scanPackage;
    }
    
    public void setTableMode(String mode)
    {
        tableMode = mode;
    }
    
    @Override
    public SqlSession getOrCreateCurrentSession()
    {
        SqlSession session = getCurrentSession();
        if (session == null)
        {
            session = openSession();
            sessionLocal.set(session);
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
        Connection connection = session.getConnection();
        for (Dao<?> dao : daos.values())
        {
            dao.deleteAll(connection);
        }
        session.commit();
        session.close();
    }
    
}
