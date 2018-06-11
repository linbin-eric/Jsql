package com.jfireframework.sql;

import java.sql.SQLException;
import java.util.IdentityHashMap;
import javax.sql.DataSource;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.curd.CurdInfo;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.executor.SqlInvoker;
import com.jfireframework.sql.mapper.Mapper;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.session.impl.SqlSessionImpl;

public class SessionFactoryImpl implements SessionFactory
{
	private final IdentityHashMap<Class<?>, Mapper>			mappers;
	private final IdentityHashMap<Class<?>, CurdInfo<?>>	curdInfos;
	private final SqlInvoker								invoker;
	private final DataSource								dataSource;
	private final Dialect									dialect;
	
	public SessionFactoryImpl(IdentityHashMap<Class<?>, Mapper> mappers, IdentityHashMap<Class<?>, CurdInfo<?>> curdInfos, SqlInvoker invoker, DataSource dataSource, Dialect dialect)
	{
		this.mappers = mappers;
		this.curdInfos = curdInfos;
		this.invoker = invoker;
		this.dataSource = dataSource;
		this.dialect = dialect;
		for (Mapper mapper : mappers.values())
		{
			mapper.setSessionFactory(this);
		}
	}
	
	@Override
	public SqlSession getCurrentSession()
	{
		return CURRENT_SESSION.get();
	}
	
	@Override
	public SqlSession openSession()
	{
		try
		{
			SqlSession session = new SqlSessionImpl(dataSource.getConnection(), invoker, curdInfos, dialect);
			return session;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public SqlSession getOrCreateCurrentSession()
	{
		SqlSession session = getCurrentSession();
		if (session == null)
		{
			session = openSession();
			CURRENT_SESSION.set(session);
		}
		return session;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getMapper(Class<T> entityClass)
	{
		try
		{
			return (T) mappers.get(entityClass);
		}
		catch (Exception e)
		{
			throw new JustThrowException(e);
		}
	}
	
}
