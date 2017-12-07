package com.jfireframework.sql.session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.constant.PkType;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.interceptor.InterceptorChain;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;
import com.jfireframework.sql.util.ExecuteSqlAndParams;

public class ExecSqlTemplate
{
	
	interface ExecStatement<T>
	{
		PreparedStatement exec(String sql, Dialect dialect, Object... params) throws Exception;
		
		T returnResult();
		
	}
	
	private static <T> T execSql(ExecStatement<T> execStatement, Dialect dialect, SqlInterceptor[] interceptors, Connection connection, String sql, Object... params)
	{
		PreparedStatement pstat = null;
		try
		{
			if (interceptors.length != 0)
			{
				InterceptorChain chain = new InterceptorChain(interceptors);
				if (chain.intercept(connection, sql, params))
				{
					sql = chain.getSql();
					params = chain.getParams();
				}
				else
				{
					return chain.getResult();
				}
			}
			pstat = execStatement.exec(sql, dialect, params);
			return execStatement.returnResult();
		}
		catch (Exception e)
		{
			throw new JustThrowException(e);
		}
		finally
		{
			try
			{
				if (pstat != null)
				{
					pstat.close();
				}
			}
			catch (SQLException e)
			{
				throw new JustThrowException(e);
			}
		}
	}
	
	public static Integer count(Dialect dialect, SqlInterceptor[] interceptors, final Connection connection, String sql, Object... params)
	{
		return (Integer) execSql(new ExecStatement<Integer>() {
			Integer result;
			
			@Override
			public Integer returnResult()
			{
				return result;
			}
			
			@Override
			public PreparedStatement exec(String sql, Dialect dialect, Object... params) throws Exception
			{
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				dialect.fillStatement(preparedStatement, params);
				ResultSet executeQuery = preparedStatement.executeQuery();
				executeQuery.next();
				result = executeQuery.getInt(1);
				return preparedStatement;
			}
		}, dialect, interceptors, connection, sql, params);
	}
	
	public static Integer insert(Dialect dialect, SqlInterceptor[] interceptors, final Connection connection, String sql, Object... params)
	{
		return (Integer) execSql(new ExecStatement<Integer>() {
			private Integer updated;
			
			@Override
			public PreparedStatement exec(String sql, Dialect dialect, Object... params) throws SQLException
			{
				PreparedStatement pstat = connection.prepareStatement(sql);
				dialect.fillStatement(pstat, params);
				updated = pstat.executeUpdate();
				return pstat;
			}
			
			@Override
			public Integer returnResult()
			{
				return updated;
			}
			
		}, dialect, interceptors, connection, sql, params);
	}
	
	public static Object databasePkGenerateInsert(Dialect dialect, final PkType idType, final String[] pkName, SqlInterceptor[] interceptors, final Connection connection, String sql, Object... params)
	{
		return execSql(new ExecStatement<Object>() {
			private Object pk;
			
			@Override
			public Object returnResult()
			{
				return pk;
			}
			
			@Override
			public PreparedStatement exec(String sql, Dialect dialect, Object... params) throws SQLException
			{
				PreparedStatement pstat = connection.prepareStatement(sql, pkName);
				dialect.fillStatement(pstat, params);
				pstat.executeUpdate();
				ResultSet resultSet = pstat.getGeneratedKeys();
				if (resultSet.next())
				{
					switch (idType)
					{
						case INT:
							pk = resultSet.getInt(1);
							break;
						case LONG:
							pk = resultSet.getLong(1);
							break;
						case STRING:
							pk = resultSet.getString(1);
							break;
						default:
							throw new UnsupportedOperationException();
					}
				}
				return pstat;
			}
		}, dialect, interceptors, connection, sql, params);
	}
	
	public static <T> List<T> pageQuery(Dialect dialect, SqlInterceptor[] interceptors, final PageParse parse, final Page pageStore, final ResultSetTransfer<T> transfer, final Connection connection, String sql, Object... params)
	{
		return (List<T>) execSql(new ExecStatement<List<T>>() {
			List<T> result;
			
			@Override
			public List<T> returnResult()
			{
				return result;
			}
			
			@Override
			public PreparedStatement exec(String sql, Dialect dialect, Object... params) throws Exception
			{
				if (pageStore.isFetchSum())
				{
					ExecuteSqlAndParams[] executeSqlAndParams = parse.parseQuery(pageStore, sql, params);
					ExecuteSqlAndParams query = executeSqlAndParams[0];
					ExecuteSqlAndParams count = executeSqlAndParams[1];
					PreparedStatement prepareStatement = connection.prepareStatement(query.getSql());
					dialect.fillStatement(prepareStatement, query.getParams());
					ResultSet executeQuery = prepareStatement.executeQuery();
					result = (List<T>) transfer.transferList(executeQuery);
					pageStore.setData(result);
					prepareStatement.close();
					prepareStatement = connection.prepareStatement(count.getSql());
					dialect.fillStatement(prepareStatement, count.getParams());
					executeQuery = prepareStatement.executeQuery();
					executeQuery.next();
					int total = executeQuery.getInt(1);
					pageStore.setTotal(total);
					return prepareStatement;
				}
				else
				{
					ExecuteSqlAndParams query = parse.parseQeuryWithoutCount(pageStore, sql, params);
					PreparedStatement prepareStatement = connection.prepareStatement(query.getSql());
					dialect.fillStatement(prepareStatement, query.getParams());
					ResultSet executeQuery = prepareStatement.executeQuery();
					result = (List<T>) transfer.transferList(executeQuery);
					pageStore.setData(result);
					return prepareStatement;
				}
			}
		}, dialect, interceptors, connection, sql, params);
	}
	
	public static <T> T queryOne(Dialect dialect, SqlInterceptor[] interceptors, final ResultSetTransfer<T> transfer, final Connection connection, String sql, Object... params)
	{
		return (T) execSql(new ExecStatement<T>() {
			T result;
			
			@Override
			public T returnResult()
			{
				return result;
			}
			
			@Override
			public PreparedStatement exec(String sql, Dialect dialect, Object... params) throws Exception
			{
				PreparedStatement pstat = connection.prepareStatement(sql);
				dialect.fillStatement(pstat, params);
				ResultSet resultSet = pstat.executeQuery();
				result = transfer.transfer(resultSet);
				return pstat;
			}
		}, dialect, interceptors, connection, sql, params);
	}
	
	public static <T> List<T> queryList(Dialect dialect, SqlInterceptor[] interceptors, final ResultSetTransfer<T> transfer, final Connection connection, String sql, Object... params)
	{
		return (List<T>) execSql(new ExecStatement<List<T>>() {
			List<T> result;
			
			@Override
			public List<T> returnResult()
			{
				return result;
			}
			
			@Override
			public PreparedStatement exec(String sql, Dialect dialect, Object... params) throws Exception
			{
				PreparedStatement pstat = connection.prepareStatement(sql);
				dialect.fillStatement(pstat, params);
				ResultSet resultSet = pstat.executeQuery();
				result = transfer.transferList(resultSet);
				return pstat;
			}
		}, dialect, interceptors, connection, sql, params);
	}
	
	public static Integer update(Dialect dialect, SqlInterceptor[] interceptors, final Connection connection, String sql, Object... params)
	{
		return (Integer) execSql(new ExecStatement<Integer>() {
			Integer result;
			
			@Override
			public Integer returnResult()
			{
				return result;
			}
			
			@Override
			public PreparedStatement exec(String sql, Dialect dialect, Object... params) throws Exception
			{
				PreparedStatement pstat = connection.prepareStatement(sql);
				dialect.fillStatement(pstat, params);
				result = pstat.executeUpdate();
				return pstat;
			}
		}, dialect, interceptors, connection, sql, params);
	}
	
}
