package com.jfireframework.sql.session.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.sql.dao.LockMode;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;
import com.jfireframework.sql.session.ExecSqlTemplate;
import com.jfireframework.sql.session.SessionFactory;
import com.jfireframework.sql.session.SqlSession;

public class SqlSessionImpl implements SqlSession
{
    private int                    transNum = 0;
    private boolean                closed   = false;
    private final Connection       connection;
    private final SessionFactory   sessionFactory;
    private final static Logger    logger   = LoggerFactory.getLogger(SqlSession.class);
    private final SqlInterceptor[] sqlInterceptors;
    private final PageParse        pageParse;
    
    public SqlSessionImpl(Connection connection, SessionFactory sessionFactory, SqlInterceptor[] sqlInterceptors, PageParse pageParse)
    {
        logger.trace("打开sqlsession");
        this.connection = connection;
        this.sessionFactory = sessionFactory;
        this.sqlInterceptors = sqlInterceptors;
        this.pageParse = pageParse;
        SessionFactory.CURRENT_SESSION.set(this);
    }
    
    @Override
    public void beginTransAction(int isolate)
    {
        try
        {
            if (transNum == 0)
            {
                transNum++;
                if (isolate > 0)
                {
                    connection.setTransactionIsolation(isolate);
                }
                connection.setAutoCommit(false);
            }
            else
            {
                transNum++;
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void commit()
    {
        try
        {
            transNum--;
            if (transNum == 0)
            {
                connection.commit();
                connection.setAutoCommit(true);
            }
        }
        catch (SQLException e)
        {
            logger.error("事务提交出现异常，请确认当前连接是否仍然还在事务内。请不要在一个事务内开启两个连接");
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void flush()
    {
        try
        {
            connection.commit();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void rollback()
    {
        try
        {
            transNum--;
            if (transNum == 0)
            {
                connection.rollback();
                connection.setAutoCommit(true);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void close()
    {
        if (closed || transNum > 0)
        {
            return;
        }
        try
        {
            closed = true;
            SessionFactory.CURRENT_SESSION.remove();
            connection.close();
            logger.trace("关闭session");
        }
        catch (SQLException e)
        {
            throw new RuntimeException("关闭", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> int delete(T entity)
    {
        return sessionFactory.getDao((Class<T>) entity.getClass()).delete(entity, this);
    }
    
    @Override
    public <T> T get(Class<T> entityClass, Object pk)
    {
        return sessionFactory.getDao(entityClass).getById(pk, this);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> void save(T entity)
    {
        sessionFactory.getDao((Class<T>) entity.getClass()).save(entity, this);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> void batchInsert(List<T> entitys)
    {
        sessionFactory.getDao((Class<T>) entitys.get(0).getClass()).batchInsert(entitys, this);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> void insert(T entity)
    {
        sessionFactory.getDao((Class<T>) entity.getClass()).insert(entity, this);
    }
    
    @Override
    public Connection getConnection()
    {
        return connection;
    }
    
    @Override
    public <T> T get(Class<T> entityClass, Object pk, LockMode mode)
    {
        return sessionFactory.getDao(entityClass).getById(pk, this, mode);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T query(ResultSetTransfer transfer, String sql, Object... params)
    {
        return (T) ExecSqlTemplate.exec(ExecSqlTemplate.query, sqlInterceptors, transfer, connection, sql, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> queryList(ResultSetTransfer transfer, String sql, Object... params)
    {
        return (List<T>) ExecSqlTemplate.exec(ExecSqlTemplate.queryList, sqlInterceptors, transfer, connection, sql, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> queryList(ResultSetTransfer transfer, String sql, Page page, Object... params)
    {
        return (List<T>) ExecSqlTemplate.exec(sqlInterceptors, pageParse, page, transfer, connection, sql, params);
    }
    
    @Override
    public int update(String sql, Object... params)
    {
        return (Integer) ExecSqlTemplate.exec(ExecSqlTemplate.update, sqlInterceptors, null, connection, sql, params);
    }
    
    @Override
    public int update(Class<?> ckass, String strategy, Object... params)
    {
        return sessionFactory.getDao(ckass).update(this, strategy, params);
    }
    
    @Override
    public <T> T findOne(Class<T> entityClass, String strategy, Object... params)
    {
        return sessionFactory.getDao(entityClass).findOne(this, strategy, params);
    }
    
    @Override
    public <T> List<T> findAll(Class<T> entityClass, String strategy, Object... params)
    {
        return sessionFactory.getDao(entityClass).findAll(this, strategy, params);
    }
    
    @Override
    public <T> List<T> findPage(Class<T> entityClass, Page page, String strategy, Object... params)
    {
        return sessionFactory.getDao(entityClass).findPage(this, page, strategy, params);
    }
    
    @Override
    public int delete(Class<?> ckass, String strategy, Object... params)
    {
        return sessionFactory.getDao(ckass).delete(this, strategy, params);
    }
    
    @Override
    public int count(Class<?> ckass, String strategy, Object... params)
    {
        return sessionFactory.getDao(ckass).count(this, strategy, params);
    }
    
}
