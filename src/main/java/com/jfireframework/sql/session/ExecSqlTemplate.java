package com.jfireframework.sql.session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.interceptor.InterceptorChain;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;
import com.jfireframework.sql.util.ExecuteSqlAndParams;
import com.jfireframework.sql.util.PkType;

public class ExecSqlTemplate
{
	
	interface ExecStatement
	{
		PreparedStatement exec(String sql, Object... params) throws Exception;
		
		Object returnResult();
		
	}
	
	private static Object execSql(ExecStatement execStatement, SqlInterceptor[] interceptors, Connection connection, String sql, Object... params)
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
			pstat = execStatement.exec(sql, params);
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
	
	public static Integer count(SqlInterceptor[] interceptors, final Connection connection, String sql, Object... params)
	{
		return (Integer) execSql(new ExecStatement() {
			Integer result;
			
			@Override
			public Object returnResult()
			{
				return result;
			}
			
			@Override
			public PreparedStatement exec(String sql, Object... params) throws Exception
			{
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				int index = 1;
				for (Object each : params)
				{
					preparedStatement.setObject(index, each);
					index += 1;
				}
				ResultSet executeQuery = preparedStatement.executeQuery();
				executeQuery.next();
				result = executeQuery.getInt(1);
				return preparedStatement;
			}
		}, interceptors, connection, sql, params);
	}
	
	public static void insert(SqlInterceptor[] interceptors, final Connection connection, String sql, Object... params)
	{
		execSql(new ExecStatement() {
			
			@Override
			public PreparedStatement exec(String sql, Object... params) throws SQLException
			{
				PreparedStatement pstat = connection.prepareStatement(sql);
				int index = 1;
				for (Object each : params)
				{
					pstat.setObject(index++, each);
				}
				pstat.executeUpdate();
				return pstat;
			}
			
			@Override
			public Object returnResult()
			{
				return null;
			}
			
		}, interceptors, connection, sql, params);
	}
	
	public static Object databasePkGenerateInsert(final PkType idType, final String[] pkName, SqlInterceptor[] interceptors, final Connection connection, String sql, Object... params)
	{
		return execSql(new ExecStatement() {
			private Object pk;
			
			@Override
			public Object returnResult()
			{
				return pk;
			}
			
			@Override
			public PreparedStatement exec(String sql, Object... params) throws SQLException
			{
				PreparedStatement pstat = connection.prepareStatement(sql, pkName);
				int index = 1;
				for (Object each : params)
				{
					pstat.setObject(index++, each);
				}
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
		}, interceptors, connection, sql, params);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> pageQuery(SqlInterceptor[] interceptors, final PageParse parse, final Page pageStore, final ResultSetTransfer transfer, final Connection connection, String sql, Object... params)
	{
		return (List<T>) execSql(new ExecStatement() {
			List<T> result;
			
			@Override
			public Object returnResult()
			{
				return result;
			}
			
			@Override
			public PreparedStatement exec(String sql, Object... params) throws Exception
			{
				if (pageStore.isFetchSum())
				{
					ExecuteSqlAndParams[] executeSqlAndParams = parse.parseQuery(pageStore, sql, params);
					ExecuteSqlAndParams query = executeSqlAndParams[0];
					ExecuteSqlAndParams count = executeSqlAndParams[1];
					PreparedStatement prepareStatement = connection.prepareStatement(query.getSql());
					int index = 1;
					for (Object param : query.getParams())
					{
						prepareStatement.setObject(index, param);
						index += 1;
					}
					ResultSet executeQuery = prepareStatement.executeQuery();
					result = (List<T>) transfer.transferList(executeQuery);
					pageStore.setData(result);
					prepareStatement.close();
					prepareStatement = connection.prepareStatement(count.getSql());
					index = 1;
					for (Object param : count.getParams())
					{
						prepareStatement.setObject(index, param);
						index += 1;
					}
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
					int index = 1;
					for (Object param : query.getParams())
					{
						prepareStatement.setObject(index, param);
						index += 1;
					}
					ResultSet executeQuery = prepareStatement.executeQuery();
					result = (List<T>) transfer.transferList(executeQuery);
					pageStore.setData(result);
					return prepareStatement;
				}
			}
		}, interceptors, connection, sql, params);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T queryOne(SqlInterceptor[] interceptors, final ResultSetTransfer transfer, final Connection connection, String sql, Object... params)
	{
		return (T) execSql(new ExecStatement() {
			Object result;
			
			@Override
			public Object returnResult()
			{
				return result;
			}
			
			@Override
			public PreparedStatement exec(String sql, Object... params) throws Exception
			{
				PreparedStatement pstat = connection.prepareStatement(sql);
				int index = 1;
				for (Object each : params)
				{
					pstat.setObject(index++, each);
				}
				ResultSet resultSet = pstat.executeQuery();
				result = transfer.transfer(resultSet);
				return pstat;
			}
		}, interceptors, connection, sql, params);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> queryList(SqlInterceptor[] interceptors, final ResultSetTransfer transfer, final Connection connection, String sql, Object... params)
	{
		return (List<T>) execSql(new ExecStatement() {
			List<Object> result;
			
			@Override
			public Object returnResult()
			{
				return result;
			}
			
			@Override
			public PreparedStatement exec(String sql, Object... params) throws Exception
			{
				PreparedStatement pstat = connection.prepareStatement(sql);
				int index = 1;
				for (Object each : params)
				{
					pstat.setObject(index++, each);
				}
				ResultSet resultSet = pstat.executeQuery();
				result = transfer.transferList(resultSet);
				return pstat;
			}
		}, interceptors, connection, sql, params);
	}
	
	public static Integer update(SqlInterceptor[] interceptors, final Connection connection, String sql, Object... params)
	{
		return (Integer) execSql(new ExecStatement() {
			Integer result;
			
			@Override
			public Object returnResult()
			{
				return result;
			}
			
			@Override
			public PreparedStatement exec(String sql, Object... params) throws Exception
			{
				PreparedStatement pstat = connection.prepareStatement(sql);
				int index = 1;
				for (Object each : params)
				{
					pstat.setObject(index++, each);
				}
				result = pstat.executeUpdate();
				return pstat;
			}
		}, interceptors, connection, sql, params);
	}
	
}
