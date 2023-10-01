package com.jfirer.jsql.session.impl;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.executor.SqlExecutor;
import com.jfirer.jsql.mapper.AbstractMapper;
import com.jfirer.jsql.mapper.MapperGenerator;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AnnotatedElement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class SqlSessionImpl implements SqlSession
{
    private              boolean     transactionActive = false;
    private              boolean     closed            = false;
    private final        Connection  connection;
    private final        SqlExecutor headSqlExecutor;
    private final        Dialect     dialect;
    private final static Logger      logger            = LoggerFactory.getLogger(SqlSession.class);

    public SqlSessionImpl(Connection connection, SqlExecutor headSqlExecutor, Dialect dialect)
    {
        this.connection      = connection;
        this.headSqlExecutor = headSqlExecutor;
        this.dialect         = dialect;
    }

    @Override
    public void beginTransAction()
    {
        checkIfClosed();
        try
        {
            if (transactionActive)
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
        if (!transactionActive)
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
        if (!transactionActive)
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
    public <T> int save(T entity)
    {
        TableEntityInfo tableEntityInfo = TableEntityInfo.parse(entity.getClass());
        if (tableEntityInfo.getPkInfo() == null || tableEntityInfo.getPkInfo().accessor().get(entity) == null)
        {
            return insert(entity);
        }
        else
        {
            return update(entity);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> int update(T entity)
    {
        Model.ModelResult result = Model.update(entity).getResult();
        return execute(result.sql(), result.paramValues());
    }

//    public <T> void insert(List<T> entities)
//    {
//        Model             model   = Model.batchInsert(entities);
//        Model.ModelResult result  = model.getResult();
//        String            sql     = result.sql();
//        List<Object>      objects = result.paramValues();
//    }

    @SuppressWarnings({"unchecked"})
    @Override
    public <T> int insert(T entity)
    {
        Model.ModelResult result = Model.insert(entity).getResult();
        if (result.pkReturnType() != TableEntityInfo.PkReturnType.NO_RETURN_PK)
        {
            String                     pk     = insertReturnPk(result.sql(), result.paramValues());
            TableEntityInfo.ColumnInfo pkInfo = TableEntityInfo.parse(entity.getClass()).getPkInfo();
            switch (result.pkReturnType())
            {
                case STRING -> pkInfo.accessor().setObject(entity, pk);
                case INT -> pkInfo.accessor().setObject(entity, Integer.valueOf(pk));
                case LONG -> pkInfo.accessor().setObject(entity, Long.valueOf(pk));
            }
            return 1;
        }
        else
        {
            return execute(result.sql(), result.paramValues());
        }
    }

    @Override
    public <T> void batchInsert(List<T> list, int batchSize)
    {
        if (list.size() > batchSize)
        {
            List<Object> batch = new LinkedList<>();
            int          count = 0;
            for (T t : list)
            {
                batch.add(t);
                if (count++ >= batchSize)
                {
                    count = 0;
                    Model.ModelResult result = Model.batchInsert(batch).getResult();
                    execute(result.sql(), result.paramValues());
                    batch.clear();
                }
            }
            if (count != 0)
            {
                Model.ModelResult result = Model.batchInsert(batch).getResult();
                execute(result.sql(), result.paramValues());
            }
        }
        else
        {
            Model.ModelResult result = Model.batchInsert(list).getResult();
            execute(result.sql(), result.paramValues());
        }
    }

    @Override
    public <T> T findOne(Model model)
    {
        Model.ModelResult result = model.getResult();
        return query(result.sql(), result.returnType(), result.paramValues());
    }

    @Override
    public <T> List<T> findList(Model model)
    {
        Model.ModelResult result = model.getResult();
        return queryList(result.sql(), result.returnType(), result.paramValues());
    }

    @Override
    public int count(Model model)
    {
        Model.ModelResult result = model.getResult();
        return query(result.sql(), Integer.class, result.paramValues());
    }

    @Override
    public int execute(Model model)
    {
        Model.ModelResult result = model.getResult();
        return execute(result.sql(), result.paramValues());
    }

    @Override
    public int execute(String sql, List<Object> params)
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
            Class<? extends AbstractMapper> ckass  = MapperGenerator.generate(mapperClass);
            AbstractMapper                  mapper = ckass.getDeclaredConstructor().newInstance();
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
