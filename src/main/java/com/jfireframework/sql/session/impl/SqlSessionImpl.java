package com.jfireframework.sql.session.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SqlSession;
import com.jfireframework.sql.curd.CurdInfo;
import com.jfireframework.sql.curd.LockMode;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.executor.SqlInvoker;
import com.jfireframework.sql.model.CountModel;
import com.jfireframework.sql.model.DeleteModel;
import com.jfireframework.sql.model.InsertModel;
import com.jfireframework.sql.model.QueryModel;
import com.jfireframework.sql.model.UpdateModel;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;
import com.jfireframework.sql.transfer.resultset.impl.IntegerTransfer;
import com.jfireframework.sql.util.TableEntityInfo;

public class SqlSessionImpl implements SqlSession
{
	private int												transNum		= 0;
	private boolean											closed			= false;
	private final Connection								connection;
	private final SqlInvoker								sqlInvoker;
	private final IdentityHashMap<Class<?>, CurdInfo<?>>	curdInfoMap;
	private final Dialect									dialect;
	private final static Logger								logger			= LoggerFactory.getLogger(SqlSession.class);
	private static final ThreadLocal<List<Object>>			cahcedParams	= new ThreadLocal<List<Object>>() {
																				protected java.util.List<Object> initialValue()
																				{
																					return new LinkedList<Object>();
																				};
																			};
	private static final ResultSetTransfer					countTransfer	= new IntegerTransfer();
	
	public SqlSessionImpl(int transNum, boolean closed, Connection connection, SqlInvoker sqlInvoker, IdentityHashMap<Class<?>, CurdInfo<?>> curdInfoMap, Dialect dialect)
	{
		this.transNum = transNum;
		this.closed = closed;
		this.connection = connection;
		this.sqlInvoker = sqlInvoker;
		this.curdInfoMap = curdInfoMap;
		this.dialect = dialect;
	}
	
	@Override
	public void beginTransAction()
	{
		try
		{
			if (transNum == 0)
			{
				transNum++;
				connection.setAutoCommit(false);
			}
			else
			{
				transNum++;
			}
		}
		catch (SQLException e)
		{
			throw new JustThrowException(e);
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
			throw new JustThrowException(e);
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
			throw new JustThrowException(e);
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
	public Connection getConnection()
	{
		return connection;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> void save(T entity)
	{
		TableEntityInfo tableEntityInfo = TableEntityInfo.parse(entity.getClass());
		Field pkField = tableEntityInfo.getPkField();
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
			throw new JustThrowException(e);
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
	public <T> T findOne(QueryModel model, Object... params)
	{
		List<Object> list = cahcedParams.get();
		for (Object each : params)
		{
			list.add(each);
		}
		String sql = model.getSql();
		T result = query(curdInfoMap.get(model.getEntityClass()).getBeanTransfer(), sql, list);
		list.clear();
		return result;
	}
	
	@Override
	public <T> List<T> find(QueryModel model, Object... params)
	{
		List<Object> list = cahcedParams.get();
		for (Object each : params)
		{
			list.add(each);
		}
		String sql = model.getSql();
		List<T> result = queryList(curdInfoMap.get(model.getEntityClass()).getBeanTransfer(), sql, list);
		list.clear();
		return result;
	}
	
	@Override
	public int update(UpdateModel model, Object... params)
	{
		List<Object> list = cahcedParams.get();
		for (Object each : params)
		{
			list.add(each);
		}
		String sql = model.getSql();
		int update = update(sql, list);
		list.clear();
		return update;
	}
	
	@Override
	public int delete(DeleteModel model, Object... params)
	{
		List<Object> list = cahcedParams.get();
		for (Object each : params)
		{
			list.add(each);
		}
		String sql = model.getSql();
		int update = update(sql, list);
		list.clear();
		return update;
	}
	
	@Override
	public int count(CountModel model, Object... params)
	{
		List<Object> list = cahcedParams.get();
		for (Object each : params)
		{
			list.add(each);
		}
		String sql = model.getSql();
		int count = query(countTransfer, sql, list);
		list.clear();
		return count;
	}
	
	@Override
	public void insert(InsertModel model, Object... params)
	{
		List<Object> list = cahcedParams.get();
		for (Object each : params)
		{
			list.add(each);
		}
		String sql = model.getSql();
		update(sql, list);
		list.clear();
	}
	
	@Override
	public int update(String sql, List<Object> params)
	{
		try
		{
			return sqlInvoker.update(sql, params, connection, dialect);
		}
		catch (SQLException e)
		{
			throw new JustThrowException(e);
		}
	}
	
	@Override
	public String insertReturnPk(String sql, List<Object> params)
	{
		try
		{
			return sqlInvoker.insertWithReturnKey(sql, params, connection, dialect);
		}
		catch (SQLException e)
		{
			throw new JustThrowException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T query(ResultSetTransfer transfer, String sql, List<Object> params)
	{
		try
		{
			return (T) sqlInvoker.queryOne(sql, params, connection, dialect, transfer);
		}
		catch (SQLException e)
		{
			throw new JustThrowException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> queryList(ResultSetTransfer transfer, String sql, List<Object> params)
	{
		try
		{
			return (List<T>) sqlInvoker.queryList(sql, params, connection, dialect, transfer);
		}
		catch (SQLException e)
		{
			throw new JustThrowException(e);
		}
	}
	
}
