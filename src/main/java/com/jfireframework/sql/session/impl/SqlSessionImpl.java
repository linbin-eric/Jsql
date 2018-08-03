package com.jfireframework.sql.session.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.curd.CurdInfo;
import com.jfireframework.sql.curd.LockMode;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.executor.SqlInvoker;
import com.jfireframework.sql.mapper.Mapper;
import com.jfireframework.sql.metadata.TableEntityInfo;
import com.jfireframework.sql.model.Model;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;
import com.jfireframework.sql.transfer.resultset.impl.IntegerTransfer;

public class SqlSessionImpl implements SqlSession
{
    private boolean                                                  transactionActive = false;
    private boolean                                                  closed            = false;
    private final IdentityHashMap<Class<?>, Class<? extends Mapper>> mappers;
    private final Connection                                         connection;
    private final SqlInvoker                                         sqlInvoker;
    private final IdentityHashMap<Class<?>, CurdInfo<?>>             curdInfoMap;
    private final Dialect                                            dialect;
    private final static Logger                                      logger            = LoggerFactory.getLogger(SqlSession.class);
    private static final ThreadLocal<List<Object>>                   cahcedParams      = new ThreadLocal<List<Object>>() {
                                                                                           protected java.util.List<Object> initialValue()
                                                                                           {
                                                                                               return new LinkedList<Object>();
                                                                                           };
                                                                                       };
    private static final ResultSetTransfer                           countTransfer     = new IntegerTransfer();
    
    public SqlSessionImpl(Connection connection, SqlInvoker sqlInvoker, IdentityHashMap<Class<?>, CurdInfo<?>> curdInfoMap, IdentityHashMap<Class<?>, Class<? extends Mapper>> mappers, Dialect dialect)
    {
        this.connection = connection;
        this.sqlInvoker = sqlInvoker;
        this.curdInfoMap = curdInfoMap;
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
        TableEntityInfo tableEntityInfo = TableEntityInfo.parse(entity.getClass());
        Field pkField = tableEntityInfo.getPkInfo().getField();
        try
        {
            if (pkField.get(entity) == null)
            {
                CurdInfo<T> curdInfo = (CurdInfo<T>) curdInfoMap.get(entity.getClass());
                List<Object> list = cahcedParams.get();
                String sql = curdInfo.autoGeneratePkInsert(entity, list);
                String pk = insertReturnPk(sql, list);
                curdInfo.setPkValue(entity, pk);
                list.clear();
            }
            else
            {
                update(entity);
            }
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> void update(T entity)
    {
        CurdInfo<T> curdInfo = (CurdInfo<T>) curdInfoMap.get(entity.getClass());
        List<Object> list = cahcedParams.get();
        String sql = curdInfo.update(entity, list);
        update(sql, list);
        list.clear();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> int delete(Class<T> ckass, Object pk)
    {
        List<Object> list = cahcedParams.get();
        CurdInfo<T> curdInfo = (CurdInfo<T>) curdInfoMap.get(ckass);
        String sql = curdInfo.delete(pk, list);
        int update = update(sql, list);
        list.clear();
        return update;
    }
    
    @SuppressWarnings({ "unchecked" })
    @Override
    public <T> void insert(T entity)
    {
        List<Object> list = cahcedParams.get();
        CurdInfo<T> curdInfo = (CurdInfo<T>) curdInfoMap.get(entity.getClass());
        String sql = curdInfo.insert(entity, list);
        update(sql, list);
        list.clear();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<T> entityClass, Object pk)
    {
        List<Object> list = cahcedParams.get();
        CurdInfo<T> curdInfo = (CurdInfo<T>) curdInfoMap.get(entityClass);
        String sql = curdInfo.find(pk, list);
        T result = query(curdInfo.getBeanTransfer(), sql, list);
        list.clear();
        return result;
    }
    
    @Override
    public <T> T get(Class<T> entityClass, Object pk, LockMode mode)
    {
        List<Object> list = cahcedParams.get();
        CurdInfo<?> curdInfo = curdInfoMap.get(entityClass);
        String sql = curdInfo.find(pk, mode, list);
        T result = query(curdInfo.getBeanTransfer(), sql, list);
        list.clear();
        return result;
    }
    
    @Override
    public <T> T findOne(Model model)
    {
        T result = query(model.getBeanTransfer(), model.getSql(), model.getParams());
        return result;
    }
    
    @Override
    public <T> List<T> find(Model model)
    {
        List<T> result = queryList(model.getBeanTransfer(), model.getSql(), model.getParams());
        return result;
    }
    
    @Override
    public int update(Model model)
    {
        int update = update(model.getSql(), model.getParams());
        return update;
    }
    
    @Override
    public int delete(Model model)
    {
        int update = update(model.getSql(), model.getParams());
        return update;
    }
    
    @Override
    public int count(Model model)
    {
        int count = query(countTransfer, model.getSql(), model.getParams());
        return count;
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
            return sqlInvoker.update(sql, params, connection, dialect);
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
            return sqlInvoker.insertWithReturnKey(sql, params, connection, dialect);
        }
        catch (SQLException e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T query(ResultSetTransfer transfer, String sql, List<Object> params)
    {
        checkIfClosed();
        try
        {
            return (T) sqlInvoker.queryOne(sql, params, connection, dialect, transfer);
        }
        catch (SQLException e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> queryList(ResultSetTransfer transfer, String sql, List<Object> params)
    {
        checkIfClosed();
        try
        {
            return (List<T>) sqlInvoker.queryList(sql, params, connection, dialect, transfer);
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
            Class<? extends Mapper> ckass = mappers.get(mapperClass);
            Mapper mapper = ckass.newInstance();
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
