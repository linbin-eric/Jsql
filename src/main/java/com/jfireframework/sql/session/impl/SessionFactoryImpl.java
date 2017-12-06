package com.jfireframework.sql.session.impl;

import java.sql.SQLException;
import java.util.IdentityHashMap;
import javax.sql.DataSource;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SqlSession;
import com.jfireframework.sql.dao.Dao;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.mapper.Mapper;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.resultsettransfer.ResultsetTransferStore;

public class SessionFactoryImpl implements SessionFactory
{
	private final IdentityHashMap<Class<?>, Mapper>	mappers;
	private final IdentityHashMap<Class<?>, Dao<?>>	daos;
	private final SqlInterceptor[]					sqlInterceptors;
	private final PageParse							pageParse;
	private final DataSource						dataSource;
	private final ResultsetTransferStore			resultsetTransferStore;
	private final Dialect							dialect;
	
	public SessionFactoryImpl(IdentityHashMap<Class<?>, Mapper> mappers, IdentityHashMap<Class<?>, Dao<?>> daos, SqlInterceptor[] sqlInterceptors, PageParse pageParse, DataSource dataSource, ResultsetTransferStore resultsetTransferStore, Dialect dialect)
	{
		this.resultsetTransferStore = resultsetTransferStore;
		this.mappers = mappers;
		this.daos = daos;
		this.sqlInterceptors = sqlInterceptors;
		this.pageParse = pageParse;
		this.dataSource = dataSource;
		this.dialect = dialect;
		for (Mapper each : mappers.values())
		{
			each.setSessionFactory(this);
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
			SqlSession session = new SqlSessionImpl(dataSource.getConnection(), this, sqlInterceptors, pageParse, dialect);
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
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Dao<T> getDao(Class<T> ckass)
	{
		return (Dao<T>) daos.get(ckass);
	}
	
	@Override
	public void cleanAllData()
	{
		SqlSession session = getOrCreateCurrentSession();
		session.beginTransAction(0);
		for (Dao<?> dao : daos.values())
		{
			dao.deleteAll(session.getConnection());
		}
		session.commit();
	}
	
	@Override
	public ResultsetTransferStore getResultSetTransferStore()
	{
		return resultsetTransferStore;
	}
	
}
