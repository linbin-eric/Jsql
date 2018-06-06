package com.jfireframework.sql.executor.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.exception.NotSingleResultException;
import com.jfireframework.sql.executor.Invoker;
import com.jfireframework.sql.executor.SqlExecutor;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

public class DefaultSqlExecutor implements SqlExecutor
{
	
	@Override
	public Object update(String sql, List<Object> params, Connection connection, Dialect dialect, Invoker next) throws SQLException
	{
		PreparedStatement prepareStatement = null;
		try
		{
			prepareStatement = connection.prepareStatement(sql);
			dialect.fillStatement(prepareStatement, params);
			int count = prepareStatement.executeUpdate();
			prepareStatement.close();
			return count;
		}
		finally
		{
			if (prepareStatement != null)
			{
				prepareStatement.close();
			}
		}
	}
	
	@Override
	public String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect, Invoker next) throws SQLException
	{
		PreparedStatement prepareStatement = null;
		ResultSet generatedKeys = null;
		try
		{
			prepareStatement = connection.prepareStatement(sql);
			dialect.fillStatement(prepareStatement, params);
			prepareStatement.executeUpdate();
			generatedKeys = prepareStatement.getGeneratedKeys();
			String pk = generatedKeys.next() ? generatedKeys.getString(1) : null;
			generatedKeys.close();
			prepareStatement.close();
			return pk;
		}
		finally
		{
			if (generatedKeys != null)
			{
				generatedKeys.close();
			}
			if (prepareStatement != null)
			{
				prepareStatement.close();
			}
		}
	}
	
	@Override
	public List<Object> queryList(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer, Invoker next) throws SQLException
	{
		PreparedStatement prepareStatement = null;
		ResultSet resultSet = null;
		try
		{
			prepareStatement = connection.prepareStatement(sql);
			dialect.fillStatement(prepareStatement, params);
			resultSet = prepareStatement.executeQuery();
			List<Object> list = new LinkedList<Object>();
			while (resultSet.next())
			{
				list.add(resultSetTransfer.transfer(resultSet));
			}
			return list;
		}
		finally
		{
			if (resultSet != null)
			{
				resultSet.close();
			}
			if (prepareStatement != null)
			{
				prepareStatement.close();
			}
		}
	}
	
	@Override
	public Object queryOne(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer, Invoker next) throws SQLException
	{
		PreparedStatement prepareStatement = null;
		ResultSet executeQuery = null;
		try
		{
			prepareStatement = connection.prepareStatement(sql);
			dialect.fillStatement(prepareStatement, params);
			executeQuery = prepareStatement.executeQuery();
			if (executeQuery.next() == false)
			{
				return null;
			}
			Object result = resultSetTransfer.transfer(executeQuery);
			if (executeQuery.next() == false)
			{
				return result;
			}
			else
			{
				throw new NotSingleResultException();
			}
		}
		finally
		{
			if (executeQuery != null)
			{
				executeQuery.close();
			}
			if (prepareStatement != null)
			{
				prepareStatement.close();
			}
		}
		
	}
	
	@Override
	public int order()
	{
		return Integer.MAX_VALUE;
	}
	
}
