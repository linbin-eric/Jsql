package com.jfireframework.sql.support.jfire.autoresource;

import java.sql.Connection;
import java.util.List;
import javax.annotation.Resource;
import com.jfireframework.baseutil.simplelog.ConsoleLogFactory;
import com.jfireframework.baseutil.simplelog.Logger;
import com.jfireframework.jfire.tx.RessourceManager;
import com.jfireframework.sql.dao.LockMode;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;
import com.jfireframework.sql.session.SessionFactory;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.util.IdType;

@Resource
public class AutoSession implements RessourceManager
{
    @Resource
    private SessionFactory sessionFactory;
    private static Logger  logger = ConsoleLogFactory.getLogger();
    
    @Override
    public void close()
    {
        logger.trace("关闭当前session{}", sessionFactory.getCurrentSession());
        SqlSession session = sessionFactory.getCurrentSession();
        if (session != null)
        {
            ((AutoSessionProxy) session).realClose();
        }
    }
    
    @Override
    public void open()
    {
        SqlSession session = sessionFactory.getOrCreateCurrentSession();
        AutoSessionProxy proxy;
        if (session instanceof AutoSessionProxy)
        {
            proxy = (AutoSessionProxy) session;
        }
        else
        {
            proxy = new AutoSessionProxy(session);
            SessionFactory.CURRENT_SESSION.set(proxy);
        }
        proxy.open();
    }
    
    class AutoSessionProxy implements SqlSession
    {
        final SqlSession host;
        int              open = 0;
        
        public void open()
        {
            open += 1;
        }
        
        public void realClose()
        {
            open -= 1;
            if (open == 0)
            {
                host.close();
            }
        }
        
        public AutoSessionProxy(SqlSession session)
        {
            this.host = session;
        }
        
        @Override
        public <T> T get(Class<T> entityClass, Object pk)
        {
            return host.get(entityClass, pk);
        }
        
        @Override
        public <T> T get(Class<T> entityClass, Object pk, LockMode mode)
        {
            return host.get(entityClass, pk, mode);
        }
        
        @Override
        public <T> void save(T entity)
        {
            host.save(entity);
        }
        
        @Override
        public <T> void batchInsert(List<T> entitys)
        {
            host.batchInsert(entitys);
        }
        
        @Override
        public <T> void insert(T entity)
        {
            host.insert(entity);
        }
        
        @Override
        public <T> int delete(T entity)
        {
            return host.delete(entity);
        }
        
        @Override
        public <T> T query(ResultSetTransfer<T> transfer, String sql, Object... params)
        {
            return host.query(transfer, sql, params);
        }
        
        @Override
        public <T> List<T> queryList(ResultSetTransfer<T> transfer, String sql, Object... params)
        {
            return host.queryList(transfer, sql, params);
        }
        
        @Override
        public <T> List<T> queryList(ResultSetTransfer<T> transfer, String sql, Page page, Object... params)
        {
            return host.queryList(transfer, sql, page, params);
        }
        
        @Override
        public int update(String sql, Object... params)
        {
            return host.update(sql, params);
        }
        
        @Override
        public void batchInsert(String sql, Object... paramArrays)
        {
            host.batchInsert(sql, paramArrays);
        }
        
        @Override
        public Object insertWithReturnPKValue(IdType idType, String[] pkName, String sql, Object... params)
        {
            return host.insertWithReturnPKValue(idType, pkName, sql, params);
        }
        
        @Override
        public void close()
        {
            ;
        }
        
        @Override
        public void beginTransAction(int isolate)
        {
            host.beginTransAction(isolate);
        }
        
        @Override
        public void commit()
        {
            host.commit();
        }
        
        @Override
        public void flush()
        {
            host.flush();
        }
        
        @Override
        public void rollback()
        {
            host.rollback();
        }
        
        @Override
        public Connection getConnection()
        {
            return host.getConnection();
        }
        
        @Override
        public <T> T findOne(Class<T> entityClass, String strategy, Object... params)
        {
            return host.findOne(entityClass, strategy, params);
        }
        
        @Override
        public <T> List<T> findAll(Class<T> entityClass, String strategy, Object... params)
        {
            return host.findAll(entityClass, strategy, params);
        }
        
        @Override
        public <T> List<T> findPage(Class<T> entityClass, Page page, String strategy, Object... params)
        {
            return host.findPage(entityClass, page, strategy, params);
        }
        
        @Override
        public int update(Class<?> ckass, String sql, Object... params)
        {
            return host.update(ckass, sql, params);
        }
        
    }
}
