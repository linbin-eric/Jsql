package com.jfireframework.sql.session.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SqlSession;
import com.jfireframework.sql.dao.LockMode;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.session.ExecSqlTemplate;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

public class SqlSessionImpl implements SqlSession
{
	private int						transNum	= 0;
	private boolean					closed		= false;
	private final Connection		connection;
	private final SessionFactory	sessionFactory;
	private final SqlInterceptor[]	sqlInterceptors;
	private final PageParse			pageParse;
	private final Dialect			dialect;
	private final static Logger		logger		= LoggerFactory.getLogger(SqlSession.class);
	
	public SqlSessionImpl(Connection connection, SessionFactory sessionFactory, SqlInterceptor[] sqlInterceptors, PageParse pageParse, Dialect dialect)
	{
		logger.trace("打开sqlsession");
		this.connection = connection;
		this.dialect = dialect;
		this.sessionFactory = sessionFactory;
		this.sqlInterceptors = sqlInterceptors;
		this.pageParse = pageParse;
		SessionFactory.CURRENT_SESSION.set(this);
	}
	
	@Override
	public void beginTransAction(int isolate)
	{
		try
		{
			if (transNum == 0)
			{
				transNum++;
				if (isolate > 0)
				{
					connection.setTransactionIsolation(isolate);
				}
				connection.setAutoCommit(false);
			}
			else
			{
				transNum++;
			}
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void commit()
	{
		try
		{
			transNum--;
			if (transNum == 0)
			{
				connection.commit();
				connection.setAutoCommit(true);
			}
		}
		catch (SQLException e)
		{
			logger.error("事务提交出现异常，请确认当前连接是否仍然还在事务内。请不要在一个事务内开启两个连接");
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void flush()
	{
		try
		{
			connection.commit();
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void rollback()
	{
		try
		{
			transNum--;
			if (transNum == 0)
			{
				connection.rollback();
				connection.setAutoCommit(true);
			}
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void close()
	{
		if (closed || transNum > 0)
		{
			return;
		}
		try
		{
			closed = true;
			SessionFactory.CURRENT_SESSION.remove();
			connection.close();
			logger.trace("关闭session");
		}
		catch (SQLException e)
		{
			throw new RuntimeException("关闭", e);
		}
	}
	
	@Override
	public <T> int delete(T entity)
	{
		return sessionFactory.getDao(entity.getClass()).delete(entity, connection);
	}
	
	@Override
	public <T> T get(Class<T> entityClass, Object pk)
	{
		return sessionFactory.getDao(entityClass).getById(pk, connection);
	}
	
	@Override
	public <T> void save(T entity)
	{
		sessionFactory.getDao(entity.getClass()).save(entity, connection);
	}
	
	@Override
	public <T> void insert(T entity)
	{
		sessionFactory.getDao(entity.getClass()).insert(entity, connection);
	}
	
	@Override
	public Connection getConnection()
	{
		return connection;
	}
	
	@Override
	public <T> T get(Class<T> entityClass, Object pk, LockMode mode)
	{
		return sessionFactory.getDao(entityClass).getById(pk, connection, mode);
	}
	
	@Override
	public <T> T query(ResultSetTransfer<T> transfer, String sql, Object... params)
	{
		return ExecSqlTemplate.queryOne(dialect, sqlInterceptors, transfer, connection, sql, params);
	}
	
	@Override
	public <T> List<T> queryList(ResultSetTransfer<T> transfer, String sql, Object... params)
	{
		return ExecSqlTemplate.queryList(dialect, sqlInterceptors, transfer, connection, sql, params);
	}
	
	@Override
	public <T> List<T> queryList(ResultSetTransfer<T> transfer, String sql, Page page, Object... params)
	{
		return ExecSqlTemplate.pageQuery(dialect, sqlInterceptors, pageParse, page, transfer, connection, sql, params);
	}
	
	@Override
	public int update(String sql, Object... params)
	{
		return ExecSqlTemplate.update(dialect, sqlInterceptors, connection, sql, params);
	}
	
	@Override
	public int update(Class<?> ckass, String strategy, Object... params)
	{
		return sessionFactory.getDao(ckass).update(connection, strategy, params);
	}
	
	@Override
	public <T> T findOne(Class<T> entityClass, String strategy, Object... params)
	{
		return sessionFactory.getDao(entityClass).findOne(connection, strategy, params);
	}
	
	@Override
	public <T> List<T> findAll(Class<T> entityClass, String strategy, Object... params)
	{
		return sessionFactory.getDao(entityClass).findAll(connection, strategy, params);
	}
	
	@Override
	public <T> List<T> findPage(Class<T> entityClass, Page page, String strategy, Object... params)
	{
		return sessionFactory.getDao(entityClass).findPage(connection, page, strategy, params);
	}
	
	@Override
	public int delete(Class<?> ckass, String strategy, Object... params)
	{
		return sessionFactory.getDao(ckass).delete(connection, strategy, params);
	}
	
	@Override
	public int count(Class<?> ckass, String strategy, Object... params)
	{
		return sessionFactory.getDao(ckass).count(connection, strategy, params);
	}
	
	@Override
	public <T> void update(T entity)
	{
		sessionFactory.getDao(entity.getClass()).update(entity, connection);
	}
	
}
