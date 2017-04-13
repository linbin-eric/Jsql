package com.jfireframework.sql.jfiresupport;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import com.jfireframework.jfire.bean.load.BeanLoadFactory;
import com.jfireframework.sql.dao.Dao;
import com.jfireframework.sql.session.SessionFactory;
import com.jfireframework.sql.session.SessionfactoryConfig;
import com.jfireframework.sql.session.SqlSession;

@Resource(name = "sessionFactory")
public class MapperLoadFactory implements BeanLoadFactory, SessionFactory
{
    private SessionFactory sessionFactory;
    @Resource
    private DataSource     dataSource;
    @Resource
    private ClassLoader    classLoader;
    private String         tableMode = "none";
    private String         scanPackage;
    
    @PostConstruct
    protected void init()
    {
        SessionfactoryConfig config = new SessionfactoryConfig();
        config.setClassLoader(classLoader);
        config.setClassLoader(classLoader);
        config.setScanPackage(scanPackage);
        config.setTableMode(tableMode);
        sessionFactory = config.build();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T, E extends T> E load(Class<T> ckass)
    {
        return (E) sessionFactory.getMapper(ckass);
    }
    
    @Override
    public <T> Dao<T> getDao(Class<T> ckass)
    {
        return sessionFactory.getDao(ckass);
    }
    
    @Override
    public <T> T getMapper(Class<T> entityClass)
    {
        return sessionFactory.getMapper(entityClass);
    }
    
    @Override
    public SqlSession getCurrentSession()
    {
        return sessionFactory.getCurrentSession();
    }
    
    @Override
    public SqlSession getOrCreateCurrentSession()
    {
        return sessionFactory.getOrCreateCurrentSession();
    }
    
    @Override
    public SqlSession openSession()
    {
        return sessionFactory.openSession();
    }
    
    @Override
    public void cleanAllData()
    {
        sessionFactory.cleanAllData();
    }
    
}
