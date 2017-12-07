package com.jfireframework.sql.dao.impl;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.dao.EntityOperator;
import com.jfireframework.sql.dbstructure.column.MapColumn;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.session.ExecSqlTemplate;
import com.jfireframework.sql.transfer.resultset.impl.BeanTransfer;

public class EntityOperatorImpl<T> implements EntityOperator<T>
{
	class FindStrategySql
	{
		String			sql;
		BeanTransfer<T>	transfer;
	}
	
	private Class<T>								ckass;
	private TableMetaData<T>						metaData;
	private ConcurrentMap<String, FindStrategySql>	findForUpdateMap	= new ConcurrentHashMap<String, EntityOperatorImpl<T>.FindStrategySql>();
	private ConcurrentMap<String, FindStrategySql>	findForShareMap		= new ConcurrentHashMap<String, EntityOperatorImpl<T>.FindStrategySql>();
	private ConcurrentMap<String, FindStrategySql>	findMap				= new ConcurrentHashMap<String, EntityOperatorImpl<T>.FindStrategySql>();
	private ConcurrentMap<String, String>			updateMap			= new ConcurrentHashMap<String, String>();
	private ConcurrentMap<String, String>			deleteMap			= new ConcurrentHashMap<String, String>();
	private ConcurrentMap<String, String>			countMap			= new ConcurrentHashMap<String, String>();
	private ConcurrentMap<String, String>			insertMap			= new ConcurrentHashMap<String, String>();
	private String									tableName;
	private SqlInterceptor[]						sqlInterceptors;
	private Dialect									dialect;
	private SessionfactoryConfig					config;
	private PageParse								pageParse;
	
	@Override
	public void initialize(TableMetaData<T> metaData, SessionfactoryConfig config, SqlInterceptor[] sqlInterceptors, String tableName, PageParse pageParse, Dialect dialect)
	{
		this.ckass = metaData.getEntityClass();
		this.dialect = dialect;
		this.config = config;
		this.metaData = metaData;
		this.tableName = tableName;
		this.sqlInterceptors = sqlInterceptors;
		this.pageParse = pageParse;
	}
	
	private FindStrategySql getFind(String strategy)
	{
		FindStrategySql findStrategySql = findMap.get(strategy);
		if (findStrategySql == null)
		{
			findStrategySql = buildFind(strategy);
			findMap.putIfAbsent(strategy, findStrategySql);
		}
		return findStrategySql;
	}
	
	private String getUpdate(String strategy)
	{
		String updateStrategySql = updateMap.get(strategy);
		if (updateStrategySql == null)
		{
			updateStrategySql = buildUpdate(strategy);
			updateMap.putIfAbsent(strategy, updateStrategySql);
		}
		return updateStrategySql;
	}
	
	private String getDelete(String strategy)
	{
		String delete = deleteMap.get(strategy);
		if (delete == null)
		{
			delete = buildDelete(strategy);
			deleteMap.putIfAbsent(strategy, delete);
		}
		return delete;
	}
	
	private String buildDelete(String strategy)
	{
		StringCache cache = new StringCache();
		cache.append("delete from ").append(tableName).append(" where ");
		for (String field : strategy.split(","))
		{
			Verify.notNull(metaData.getColumn(field), "策略:{}中的字段:{}不存在", strategy, field);
			cache.append(metaData.getColumn(field).getColName()).append("=? and ");
		}
		return cache.deleteEnds(4).toString();
	}
	
	private FindStrategySql buildFind(String fields)
	{
		StringCache cache = new StringCache();
		cache.append("select ");
		String[] tmp = fields.split(";");
		String valueFields = tmp[0];
		String whereFields = tmp.length > 1 ? tmp[1] : null;
		String orderFields = tmp.length > 2 ? tmp[2] : null;
		if (valueFields.equals("*"))
		{
			for (MapColumn each : metaData.getAllColumns().values())
			{
				cache.append(each.getColName()).appendComma();
			}
		}
		else
		{
			for (String selectField : valueFields.split(","))
			{
				if (StringUtil.isNotBlank(selectField) == false)
				{
					continue;
				}
				Verify.notNull(metaData.getColumn(selectField), "策略:{}中的字段:{}不存在", fields, selectField);
				cache.append(metaData.getColumn(selectField).getColName()).appendComma();
			}
			if (StringUtil.isNotBlank(whereFields))
			{
				for (String whereField : whereFields.split(","))
				{
					Verify.notNull(metaData.getColumn(whereField), "策略:{}中的字段:{}不存在", fields, whereField);
					cache.append(metaData.getColumn(whereField).getColName()).appendComma();
				}
			}
		}
		cache.deleteLast().append(" from ").append(tableName);
		if (StringUtil.isNotBlank(whereFields))
		{
			cache.append(" where ");
			for (String whereField : whereFields.split(","))
			{
				Verify.notNull(metaData.getColumn(whereField), "策略:{}中的字段:{}不存在", fields, whereField);
				cache.append(metaData.getColumn(whereField).getColName()).append("=? and ");
			}
			cache.deleteEnds(4);
		}
		if (StringUtil.isNotBlank(orderFields))
		{
			cache.append(" order by ");
			for (String each : orderFields.split(","))
			{
				if (each.contains(":"))
				{
					String[] orderRule = each.split(":");
					Verify.notNull(metaData.getColumn(orderRule[0]).getColName(), "策略:{}中的字段:{}不存在", fields, orderRule[0]);
					cache.append(metaData.getColumn(orderRule[0]).getColName()).append(" ").append(orderRule[1]).append(",");
					if ("aes".equals(orderRule[1]) || "desc".equals(orderRule[1]))
					{
						;
					}
					else
					{
						throw new UnsupportedOperationException(StringUtil.format("策略:{}中排序内容:{} 错误", fields, orderRule[1]));
					}
				}
				else
				{
					Verify.notNull(metaData.getColumn(each), "策略:{}中的字段:{}不存在", fields, each);
					cache.append(metaData.getColumn(each)).append(",");
				}
			}
			cache.deleteLast();
		}
		FindStrategySql findStrategySql = new FindStrategySql();
		findStrategySql.sql = cache.toString();
		findStrategySql.transfer = new BeanTransfer<T>();
		findStrategySql.transfer.initialize(ckass, config);
		return findStrategySql;
	}
	
	private String buildUpdate(String fields)
	{
		StringCache cache = new StringCache();
		cache.append("update ").append(tableName).append(" set ");
		String[] tmp = fields.split(";");
		String valueFields = tmp[0];
		String whereFields = tmp[1];
		for (String setField : valueFields.split(","))
		{
			Verify.notNull(metaData.getColumn(setField), "策略:{}中的字段:{}不存在", fields, setField);
			cache.append(metaData.getColumn(setField).getColName()).append("=?,");
		}
		cache.deleteLast().append(" where ");
		for (String whereField : whereFields.split(","))
		{
			Verify.notNull(metaData.getColumn(whereField), "策略:{}中的字段:{}不存在", fields, whereField);
			cache.append(metaData.getColumn(whereField).getColName()).append("=? and ");
		}
		return cache.deleteEnds(4).toString();
	}
	
	@Override
	public int update(Connection connection, String strategy, Object... params)
	{
		String strategySql = getUpdate(strategy);
		return ExecSqlTemplate.update(dialect, sqlInterceptors, connection, strategySql, params);
	}
	
	@Override
	public T findOne(Connection connection, String strategy, Object... params)
	{
		FindStrategySql strategySql = getFind(strategy);
		return ExecSqlTemplate.queryOne(dialect, sqlInterceptors, strategySql.transfer, connection, strategySql.sql, params);
	}
	
	@Override
	public List<T> findAll(Connection connection, String strategy, Object... params)
	{
		FindStrategySql strategySql = getFind(strategy);
		return ExecSqlTemplate.queryList(dialect, sqlInterceptors, strategySql.transfer, connection, strategySql.sql, params);
	}
	
	@Override
	public List<T> findPage(Connection connection, Page page, String strategy, Object... params)
	{
		FindStrategySql strategySql = getFind(strategy);
		return ExecSqlTemplate.pageQuery(dialect, sqlInterceptors, pageParse, page, strategySql.transfer, connection, strategySql.sql, params);
	}
	
	@Override
	public int delete(Connection connection, String strategy, Object... params)
	{
		String delete = getDelete(strategy);
		return ExecSqlTemplate.update(dialect, sqlInterceptors, connection, delete, params);
	}
	
	@Override
	public int count(Connection connection, String strategy, Object... params)
	{
		String count = getCount(strategy);
		return ExecSqlTemplate.count(dialect, sqlInterceptors, connection, count, params);
	}
	
	private String getCount(String strategy)
	{
		String sql = countMap.get(strategy);
		if (sql == null)
		{
			sql = buildCount(strategy);
			countMap.putIfAbsent(strategy, sql);
		}
		return sql;
	}
	
	private String buildCount(String strategy)
	{
		StringCache cache = new StringCache();
		cache.append("select count(*) from ").append(tableName);
		if ("".equals(strategy))
		{
			return cache.toString();
		}
		else
		{
			cache.append(" where ");
			for (String whereField : strategy.split(","))
			{
				Verify.notNull(metaData.getColumn(whereField), "策略:{}中的字段:{}不存在", strategy, whereField);
				cache.append(metaData.getColumn(whereField).getColName()).append("=? and ");
			}
			return cache.deleteEnds(4).toString();
		}
	}
	
	@Override
	public int insert(Connection connection, String strategy, Object... params)
	{
		String sql = getInsert(strategy);
		return ExecSqlTemplate.insert(dialect, sqlInterceptors, connection, sql, params);
	}
	
	private String getInsert(String strategy)
	{
		String sql = insertMap.get(strategy);
		if (sql == null)
		{
			sql = buildInsert(strategy);
			insertMap.put(strategy, sql);
		}
		return sql;
	}
	
	private String buildInsert(String strategy)
	{
		StringCache cache = new StringCache();
		
		cache.append("INSERT INTO ").append(tableName).append('(');
		int count = 0;
		for (String each : strategy.split(","))
		{
			MapColumn mapField = metaData.getColumn(each);
			cache.append(mapField.getColName()).appendComma();
			count += 1;
		}
		cache.deleteLast().append(") VALUES(");
		for (int i = 0; i < count; i++)
		{
			cache.append("?").appendComma();
		}
		cache.deleteLast().append(")");
		return cache.toString();
	}
	
	@Override
	public T findOneForUpdate(Connection connection, String strategy, Object... params)
	{
		FindStrategySql sqlForUpdate = getFindForUpdate(strategy);
		return ExecSqlTemplate.queryOne(dialect, sqlInterceptors, sqlForUpdate.transfer, connection, sqlForUpdate.sql, params);
	}
	
	private FindStrategySql getFindForUpdate(String strategy)
	{
		FindStrategySql findStrategySql = findForUpdateMap.get(strategy);
		if (findStrategySql == null)
		{
			findStrategySql = buildFind(strategy);
			findStrategySql.sql = findStrategySql.sql + " FOR UPDATE";
			findForUpdateMap.putIfAbsent(strategy, findStrategySql);
		}
		return findStrategySql;
	}
	
	@Override
	public T findOneForShare(Connection connection, String strategy, Object... params)
	{
		FindStrategySql sqlForShare = getFindForShare(strategy);
		return ExecSqlTemplate.queryOne(dialect, sqlInterceptors, sqlForShare.transfer, connection, sqlForShare.sql, params);
	}
	
	private FindStrategySql getFindForShare(String strategy)
	{
		FindStrategySql findStrategySql = findForShareMap.get(strategy);
		if (findStrategySql == null)
		{
			findStrategySql = buildFind(strategy);
			findStrategySql.sql = findStrategySql.sql + " LOCK IN SHARE MODE";
			findForShareMap.putIfAbsent(strategy, findStrategySql);
		}
		return findStrategySql;
	}
}
