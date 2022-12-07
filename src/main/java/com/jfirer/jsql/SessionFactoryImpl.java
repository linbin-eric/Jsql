package com.jfirer.jsql;

import com.jfirer.jsql.curd.CurdInfo;
import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.executor.SqlExecutor;
import com.jfirer.jsql.mapper.AbstractMapper;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.session.impl.SqlSessionImpl;
import com.jfirer.baseutil.reflect.ReflectUtil;

import javax.sql.DataSource;
import java.util.IdentityHashMap;

public class SessionFactoryImpl implements SessionFactory
{
    private final IdentityHashMap<Class<?>, Class<? extends AbstractMapper>> mappers;
    private final IdentityHashMap<Class<?>, CurdInfo<?>> curdInfos;
    private final SqlExecutor                            headSqlExecutor;
    private final DataSource                             dataSource;
    private final Dialect                                            dialect;

    public SessionFactoryImpl(IdentityHashMap<Class<?>, Class<? extends AbstractMapper>> mappers, IdentityHashMap<Class<?>, CurdInfo<?>> curdInfos, SqlExecutor headSqlExecutor, DataSource dataSource, Dialect dialect)
    {
        this.mappers = mappers;
        this.curdInfos = curdInfos;
        this.headSqlExecutor = headSqlExecutor;
        this.dataSource = dataSource;
        this.dialect = dialect;
        for (CurdInfo<?> curdInfo : curdInfos.values())
        {
            curdInfo.setSessionFactory(this);
        }
    }

    @Override
    public SqlSession openSession()
    {
        try
        {
            return new SqlSessionImpl(dataSource.getConnection(), headSqlExecutor, curdInfos, mappers, dialect);
        } catch (Throwable e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

}
