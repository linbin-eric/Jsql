package com.jfireframework.sql.executor.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.executor.SqlExecutor;
import com.jfireframework.sql.executor.SqlInvoker;
import com.jfireframework.sql.metadata.Page;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

public class OraclePageExecutor implements SqlExecutor
{
	
	@Override
	public int update(String sql, List<Object> params, Connection connection, Dialect dialect, SqlInvoker next) throws SQLException
	{
		return next.update(sql, params, connection, dialect);
	}
	
	@Override
	public String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect, SqlInvoker next) throws SQLException
	{
		return next.insertWithReturnKey(sql, params, connection, dialect);
	}
	
	@Override
	public List<Object> queryList(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer, SqlInvoker next) throws SQLException
	{
		Object param = params.get(params.size() - 1);
		if (param instanceof Page == false)
		{
			return next.queryList(sql, params, connection, dialect, resultSetTransfer);
		}
		Page page = (Page) param;
		if (page.isFetchSum())
		{
			String countSql = "select count(*) from (" + sql + ")";
			PreparedStatement prepareStatement = null;
			ResultSet resultSet = null;
			try
			{
				prepareStatement = connection.prepareStatement(countSql);
				dialect.fillStatement(prepareStatement, params);
				resultSet = prepareStatement.executeQuery();
				resultSet.next();
				int total = resultSet.getInt(1);
				page.setTotal(total);
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
		sql = "select * from ( select a.*,rownum rn from(" + sql + ") a where rownum<=?) where rn>=?";
		params.remove(params.size() - 1);
		params.add(page.getOffset() + page.getSize());
		params.add(page.getOffset() + 1);
		return next.queryList(sql, params, connection, dialect, resultSetTransfer);
	}
	
	@Override
	public Object queryOne(String sql, List<Object> params, Connection connection, Dialect dialect, ResultSetTransfer resultSetTransfer, SqlInvoker next) throws SQLException
	{
		return next.queryOne(sql, params, connection, dialect, resultSetTransfer);
	}
	
	@Override
	public int order()
	{
		return 1000;
	}
	
}
