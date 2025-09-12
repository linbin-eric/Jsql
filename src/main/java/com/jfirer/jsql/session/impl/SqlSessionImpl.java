package com.jfirer.jsql.session.impl;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.executor.SqlExecutor;
import com.jfirer.jsql.mapper.AbstractMapper;
import com.jfirer.jsql.mapper.MapperGenerator;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.model.InsertEntityModel;
import com.jfirer.jsql.model.model.QueryModel;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.transfer.ResultSetTransfer;
import com.jfirer.jsql.transfer.impl.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class SqlSessionImpl implements SqlSession
{
    private              boolean                                  closed         = false;
    private              Connection                               connection;
    private final        SqlExecutor                              headSqlExecutor;
    private final        Dialect                                  dialect;
    //key：sql+类的简单名称
    private static final ConcurrentMap<String, ResultSetTransfer> transferCache  = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ResultSetTransfer>         BASIC_TRANSFER = new HashMap<>();

    static
    {
        BASIC_TRANSFER.put(Integer.class, IntegerTransfer.INSTANCE);
        BASIC_TRANSFER.put(int.class, IntegerTransfer.INSTANCE);
        BASIC_TRANSFER.put(Long.class, LongTransfer.INSTANCE);
        BASIC_TRANSFER.put(long.class, LongTransfer.INSTANCE);
        BASIC_TRANSFER.put(Float.class, FloatTransfer.INSTANCE);
        BASIC_TRANSFER.put(float.class, FloatTransfer.INSTANCE);
        BASIC_TRANSFER.put(Double.class, DoubleTransfer.INSTANCE);
        BASIC_TRANSFER.put(double.class, DoubleTransfer.INSTANCE);
        BASIC_TRANSFER.put(Boolean.class, BooleanTransfer.INSTANCE);
        BASIC_TRANSFER.put(boolean.class, BooleanTransfer.INSTANCE);
        BASIC_TRANSFER.put(String.class, StringTransfer.INSTANCE);
        BASIC_TRANSFER.put(Short.class, ShortTransfer.INSTANCE);
        BASIC_TRANSFER.put(short.class, ShortTransfer.INSTANCE);
        BASIC_TRANSFER.put(BigDecimal.class, BigDecimalTransfer.INSTANCE);
        BASIC_TRANSFER.put(Date.class, UtilDateTransfer.INSTANCE);
        BASIC_TRANSFER.put(java.sql.Date.class, SqlDateTransfer.INSTANCE);
    }

    public SqlSessionImpl(Connection connection, SqlExecutor headSqlExecutor, Dialect dialect)
    {
        this.connection      = connection;
        this.headSqlExecutor = headSqlExecutor;
        this.dialect         = dialect;
    }

    private void checkIfClosed()
    {
        if (closed)
        {
            throw new IllegalStateException("当前Session已经关闭，不能执行其他操作");
        }
    }

    @SneakyThrows
    @Override
    public void close()
    {
        if (connection != null)
        {
            if (connection.getAutoCommit()==false)
            {
                throw new IllegalStateException("还没有提交事务就关闭数据库连接");
            }
            connection.close();
            connection = null;
        }
        else
        {
            throw new IllegalStateException("已经关闭过");
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

    @SuppressWarnings({"unchecked"})
    @Override
    public <T> int insert(T entity)
    {
        InsertEntityModel            insert       = Model.insert(entity);
        Model.ModelResult            result       = insert.getResult();
        TableEntityInfo.PkReturnType pkReturnType = insert.getPkReturnType();
        if (pkReturnType != TableEntityInfo.PkReturnType.NO_RETURN_PK)
        {
            TableEntityInfo.ColumnInfo pkInfo = TableEntityInfo.parse(entity.getClass()).getPkInfo();
            String                     pk     = insertReturnPk(result.sql(), result.paramValues(), pkInfo);
            switch (pkReturnType)
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
    public <T> void batchInsert(Collection<T> collection, int batchSize)
    {
        if (collection.size() > batchSize)
        {
            List<Object> batch = new LinkedList<>();
            int          count = 0;
            for (T t : collection)
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
            Model.ModelResult result = Model.batchInsert(collection).getResult();
            execute(result.sql(), result.paramValues());
        }
    }

    @Override
    public <T> T findOne(QueryModel model)
    {
        Model.ModelResult result = model.getResult();
        String            sql    = result.sql();
        String            key    = sql + model.getReturnType().getSimpleName();
        ResultSetTransfer transfer = transferCache.computeIfAbsent(key, s -> {
            ResultSetTransfer resultSetTransfer = BASIC_TRANSFER.get(model.getReturnType());
            if (resultSetTransfer != null)
            {
                return resultSetTransfer;
            }
            else
            {
                return new BeanTransfer(model.getReturnType());
            }
        });
        return query(sql, transfer, result.paramValues());
    }

    @Override
    public <T> List<T> findList(QueryModel model)
    {
        Model.ModelResult result = model.getResult();
        String            sql    = result.sql();
        String            key    = sql + model.getReturnType().getSimpleName();
        ResultSetTransfer transfer = transferCache.computeIfAbsent(key, s -> {
            ResultSetTransfer resultSetTransfer = BASIC_TRANSFER.get(model.getReturnType());
            if (resultSetTransfer != null)
            {
                return resultSetTransfer;
            }
            else
            {
                return new BeanTransfer(model.getReturnType());
            }
        });
        return queryList(sql, transfer, result.paramValues());
    }

    @Override
    public Page findListByPage(QueryModel model)
    {
        Page page = model.getPage();
        findList(model);
        return page;
    }

    @Override
    public int count(Model model)
    {
        Model.ModelResult result = model.getResult();
        return query(result.sql(), IntegerTransfer.INSTANCE, result.paramValues());
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
    public String insertReturnPk(String sql, List<Object> params, TableEntityInfo.ColumnInfo pkInfo)
    {
        checkIfClosed();
        try
        {
            return headSqlExecutor.insertWithReturnKey(sql, params, connection, dialect, pkInfo);
        }
        catch (SQLException e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T query(String sql, ResultSetTransfer transfer, List<Object> params)
    {
        checkIfClosed();
        try
        {
            return (T) headSqlExecutor.queryOne(sql, transfer, params, connection, dialect);
        }
        catch (SQLException e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> queryList(String sql, ResultSetTransfer transfer, List<Object> params)
    {
        checkIfClosed();
        try
        {
            return (List<T>) headSqlExecutor.queryList(sql, transfer, params, connection, dialect);
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
