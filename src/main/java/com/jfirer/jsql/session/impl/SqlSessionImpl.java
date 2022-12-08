package com.jfirer.jsql.session.impl;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jsql.curd.CurdOpSupport;
import com.jfirer.jsql.curd.LockMode;
import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.executor.SqlExecutor;
import com.jfirer.jsql.mapper.AbstractMapper;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AnnotatedElement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.List;

public class SqlSessionImpl implements SqlSession
{
    private              boolean                                                    transactionActive = false;
    private              boolean                                                    closed            = false;
    private final        IdentityHashMap<Class<?>, Class<? extends AbstractMapper>> mappers;
    private final        Connection                                                 connection;
    private final        SqlExecutor                                 headSqlExecutor;
    private final        IdentityHashMap<Class<?>, CurdOpSupport<?>> curdOpSupportMap;
    private final        Dialect                                     dialect;
    private final static Logger                                                     logger            = LoggerFactory.getLogger(SqlSession.class);

    public SqlSessionImpl(Connection connection, SqlExecutor headSqlExecutor, IdentityHashMap<Class<?>, CurdOpSupport<?>> curdOpSupportMap, IdentityHashMap<Class<?>, Class<? extends AbstractMapper>> mappers, Dialect dialect)
    {
        this.connection = connection;
        this.headSqlExecutor = headSqlExecutor;
        this.curdOpSupportMap = curdOpSupportMap;
        this.dialect = dialect;
        this.mappers = mappers;
    }

    @Override
    public void beginTransAction()
    {
        checkIfClosed();
        try
        {
            if (transactionActive != false)
            {
                return;
            }
            transactionActive = true;
            connection.setAutoCommit(false);
        }
        catch (SQLException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    private void checkIfClosed()
    {
        if (closed)
        {
            throw new IllegalStateException("当前Session已经关闭，不能执行其他操作");
        }
    }

    @Override
    public void commit()
    {
        checkIfClosed();
        if (transactionActive == false)
        {
            throw new IllegalStateException("当前链接未开启事务，无法进行提交");
        }
        try
        {
            connection.commit();
            connection.setAutoCommit(true);
            transactionActive = false;
        }
        catch (SQLException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    @Override
    public void flush()
    {
        checkIfClosed();
        try
        {
            connection.commit();
        }
        catch (SQLException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    @Override
    public void rollback()
    {
        checkIfClosed();
        if (transactionActive == false)
        {
            throw new IllegalStateException("当前链接未开启事务，无需回滚");
        }
        try
        {
            connection.rollback();
            connection.setAutoCommit(true);
            transactionActive = false;
        }
        catch (SQLException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    @Override
    public void close()
    {
        if (transactionActive)
        {
            throw new IllegalStateException("当前链接仍然开启着事务，需要先执行提交");
        }
        try
        {
            closed = true;
            connection.close();
            logger.trace("关闭session");
        }
        catch (SQLException e)
        {
            throw new RuntimeException("关闭", e);
        }
    }

    @Override
    public Connection getConnection()
    {
        return connection;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void save(T entity)
    {
        CurdOpSupport<T> curdInfo = (CurdOpSupport<T>) curdOpSupportMap.get(entity.getClass());
        curdInfo.save(entity, headSqlExecutor, dialect, connection);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void update(T entity)
    {
        checkIfClosed();
        CurdOpSupport<T> curdInfo = (CurdOpSupport<T>) curdOpSupportMap.get(entity.getClass());
        curdInfo.update(entity, headSqlExecutor, dialect, connection);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> int delete(Class<T> ckass, Object pk)
    {
        checkIfClosed();
        CurdOpSupport<T> curdInfo = (CurdOpSupport<T>) curdOpSupportMap.get(ckass);
        return curdInfo.delete(pk, headSqlExecutor, dialect, connection);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public <T> void insert(T entity)
    {
        checkIfClosed();
        CurdOpSupport<T> curdInfo = (CurdOpSupport<T>) curdOpSupportMap.get(entity.getClass());
        curdInfo.insert(entity, headSqlExecutor, dialect, connection);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<T> entityClass, Object pk)
    {
        checkIfClosed();
        CurdOpSupport<T> curdInfo = (CurdOpSupport<T>) curdOpSupportMap.get(entityClass);
        return curdInfo.find(pk, headSqlExecutor, dialect, connection);
    }

    @Override
    public <T> T get(Class<T> entityClass, Object pk, LockMode mode)
    {
        checkIfClosed();
        CurdOpSupport<?> curdInfo = curdOpSupportMap.get(entityClass);
        return (T) curdInfo.find(pk, mode, headSqlExecutor, dialect, connection);
    }

    @Override
    public <T> T findOne(Model model)
    {
        return (T) query(model.getSql(), model.getEntityClass(), model.getParams());
    }

    @Override
    public <T> List<T> find(Model model)
    {
        return (List<T>) queryList(model.getSql(), model.getEntityClass(), model.getParams());
    }

    @Override
    public int update(Model model)
    {
        return update(model.getSql(), model.getParams());
    }

    @Override
    public int delete(Model model)
    {
        return update(model.getSql(), model.getParams());
    }

    @Override
    public int count(Model model)
    {
        return (int) query(model.getSql(), Integer.class, model.getParams());
    }

    @Override
    public void insert(Model model)
    {
        update(model.getSql(), model.getParams());
    }

    @Override
    public int update(String sql, List<Object> params)
    {
        checkIfClosed();
        try
        {
            return headSqlExecutor.update(sql, params, connection, dialect);
        }
        catch (SQLException e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    @Override
    public String insertReturnPk(String sql, List<Object> params)
    {
        checkIfClosed();
        try
        {
            return headSqlExecutor.insertWithReturnKey(sql, params, connection, dialect);
        }
        catch (SQLException e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T query(String sql, AnnotatedElement element, List<Object> params)
    {
        checkIfClosed();
        try
        {
            return (T) headSqlExecutor.queryOne(sql, element, params, connection, dialect);
        }
        catch (SQLException e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> queryList(String sql, AnnotatedElement element, List<Object> params)
    {
        checkIfClosed();
        try
        {
            return (List<T>) headSqlExecutor.queryList(sql, element, params, connection, dialect);
        }
        catch (SQLException e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getMapper(Class<T> mapperClass)
    {
        try
        {
            Class<? extends AbstractMapper> ckass  = mappers.get(mapperClass);
            AbstractMapper                  mapper = ckass.newInstance();
            mapper.setSession(this);
            return (T) mapper;
        }
        catch (Throwable e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }
}
