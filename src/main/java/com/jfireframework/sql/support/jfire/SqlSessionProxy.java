package com.jfireframework.sql.support.jfire;

import java.sql.Connection;
import java.util.List;
import javax.annotation.Resource;
import com.jfireframework.sql.dao.LockMode;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;
import com.jfireframework.sql.session.SessionFactory;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.util.IdType;

@Resource
public class SqlSessionProxy implements SqlSession
{
    @Resource
    private SessionFactory sessionFactory;
    
    @Override
    public <T> T get(Class<T> entityClass, Object pk)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        return session.get(entityClass, pk);
    }
    
    @Override
    public <T> T get(Class<T> entityClass, Object pk, LockMode mode)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        return session.get(entityClass, pk, mode);
    }
    
    @Override
    public <T> void save(T entity)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        session.save(entity);
    }
    
    @Override
    public <T> void batchInsert(List<T> entitys)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        session.batchInsert(entitys);
    }
    
    @Override
    public <T> void insert(T entity)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        session.insert(entity);
    }
    
    @Override
    public <T> int delete(T entity)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        return session.delete(entity);
    }
    
    @Override
    public <T> T query(ResultSetTransfer<T> transfer, String sql, Object... params)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        return session.query(transfer, sql, params);
    }
    
    @Override
    public <T> List<T> queryList(ResultSetTransfer<T> transfer, String sql, Object... params)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        return session.queryList(transfer, sql, params);
    }
    
    @Override
    public <T> List<T> queryList(ResultSetTransfer<T> transfer, String sql, Page page, Object... params)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        return session.queryList(transfer, sql, page, params);
    }
    
    @Override
    public int update(String sql, Object... params)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        return session.update(sql, params);
    }
    
    @Override
    public void batchInsert(String sql, Object... paramArrays)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        session.batchInsert(sql, paramArrays);
    }
    
    @Override
    public Object insertWithReturnPKValue(IdType idType, String[] pkName, String sql, Object... params)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        return session.insertWithReturnPKValue(idType, pkName, sql, params);
    }
    
    @Override
    public void close()
    {
        SqlSession session = sessionFactory.getCurrentSession();
        session.close();
    }
    
    @Override
    public void beginTransAction(int isolate)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        session.beginTransAction(isolate);
    }
    
    @Override
    public void commit()
    {
        SqlSession session = sessionFactory.getCurrentSession();
        session.commit();
    }
    
    @Override
    public void flush()
    {
        SqlSession session = sessionFactory.getCurrentSession();
        session.flush();
    }
    
    @Override
    public void rollback()
    {
        SqlSession session = sessionFactory.getCurrentSession();
        session.rollback();
    }
    
    @Override
    public Connection getConnection()
    {
        SqlSession session = sessionFactory.getCurrentSession();
        return session.getConnection();
    }
    
    @Override
    public <T> T findOne(Class<T> entityClass, String strategy, Object... params)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        return session.findOne(entityClass, strategy, params);
    }
    
    @Override
    public <T> List<T> findAll(Class<T> entityClass, String strategy, Object... params)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        return session.findAll(entityClass, strategy, params);
    }
    
    @Override
    public <T> List<T> findPage(Class<T> entityClass, Page page, String strategy, Object... params)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        return session.findPage(entityClass, page, strategy, params);
    }
    
    @Override
    public int update(Class<?> ckass, String sql, Object... params)
    {
        SqlSession session = sessionFactory.getCurrentSession();
        return session.update(ckass, sql, params);
    }
    
}
