package com.jfirer.jsql;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jsql.curd.CurdOpSupport;
import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.executor.SqlExecutor;
import com.jfirer.jsql.mapper.AbstractMapper;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.session.impl.SqlSessionImpl;

import javax.sql.DataSource;
import java.util.IdentityHashMap;

public class SessionFactoryImpl implements SessionFactory
{
    private final IdentityHashMap<Class<?>, Class<? extends AbstractMapper>> mappers;
    private final IdentityHashMap<Class<?>, CurdOpSupport<?>>                curdInfos;
    private final SqlExecutor                                                headSqlExecutor;
    private final DataSource                                                 dataSource;
    private final Dialect                                                    dialect;

    public SessionFactoryImpl(IdentityHashMap<Class<?>, Class<? extends AbstractMapper>> mappers, IdentityHashMap<Class<?>, CurdOpSupport<?>> curdInfos, SqlExecutor headSqlExecutor, DataSource dataSource, Dialect dialect)
    {
        this.mappers = mappers;
        this.curdInfos = curdInfos;
        this.headSqlExecutor = headSqlExecutor;
        this.dataSource = dataSource;
        this.dialect = dialect;
    }

    @Override
    public SqlSession openSession()
    {
        try
        {
            return new SqlSessionImpl(dataSource.getConnection(), headSqlExecutor, curdInfos, mappers, dialect);
        }
        catch (Throwable e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }
}
