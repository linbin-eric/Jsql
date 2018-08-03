package com.jfireframework.sql;

import java.sql.SQLException;
import java.util.IdentityHashMap;
import javax.sql.DataSource;
import com.jfireframework.sql.curd.CurdInfo;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.executor.SqlInvoker;
import com.jfireframework.sql.mapper.Mapper;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.session.impl.SqlSessionImpl;

public class SessionFactoryImpl implements SessionFactory
{
    private final IdentityHashMap<Class<?>, Class<? extends Mapper>> mappers;
    private final IdentityHashMap<Class<?>, CurdInfo<?>>             curdInfos;
    private final SqlInvoker                                         invoker;
    private final DataSource                                         dataSource;
    private final Dialect                                            dialect;
    
    public SessionFactoryImpl(IdentityHashMap<Class<?>, Class<? extends Mapper>> mappers, IdentityHashMap<Class<?>, CurdInfo<?>> curdInfos, SqlInvoker invoker, DataSource dataSource, Dialect dialect)
    {
        this.mappers = mappers;
        this.curdInfos = curdInfos;
        this.invoker = invoker;
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
            SqlSession session = new SqlSessionImpl(dataSource.getConnection(), invoker, curdInfos, mappers, dialect);
            return session;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
    
}
