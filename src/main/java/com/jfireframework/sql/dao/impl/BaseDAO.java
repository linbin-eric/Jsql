package com.jfireframework.sql.dao.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.constant.PkType;
import com.jfireframework.sql.dao.Dao;
import com.jfireframework.sql.dao.EntityOperator;
import com.jfireframework.sql.dao.LockMode;
import com.jfireframework.sql.dbstructure.column.MapColumn;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.session.ExecSqlTemplate;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;
import com.jfireframework.sql.transfer.resultset.impl.BeanTransfer;
import sun.misc.Unsafe;

public abstract class BaseDAO<T> implements Dao<T>
{
	protected class FieldValueFetcher
	{
		Field field;
		
		public FieldValueFetcher(Field field)
		{
			field.setAccessible(true);
			this.field = field;
		}
		
		Object fieldValue(Object entity)
		{
			try
			{
				return field.get(entity);
			}
			catch (Exception e)
			{
				throw new JustThrowException(e);
			}
		}
	}
	
	class ExecuteSqlContext
	{
		String				sql;
		FieldValueFetcher[]	fetchers;
		
		@SuppressWarnings("unchecked")
		public ExecuteSqlContext(String sql, Object... fetchers)
		{
			this.sql = sql;
			this.fetchers = new BaseDAO.FieldValueFetcher[fetchers.length];
			for (int i = 0; i < fetchers.length; i++)
			{
				this.fetchers[i] = (BaseDAO<T>.FieldValueFetcher) fetchers[i];
			}
		}
		
		public Object[] parseParams(Object entity)
		{
			return BaseDAO.this.parseParams(entity, fetchers);
		}
	}
	
	protected Class<T>				entityClass;
	// 数据表主键的列
	protected MapColumn				pkColumn;
	// 数据表的所有列
	protected MapColumn[]			allColumns;
	// 数据表除了主键之外所有列
	protected MapColumn[]			valueColumns;
	protected long					pkColumnOffset;
	protected PkType				pkType;
	protected String				tableName;
	protected ExecuteSqlContext		queryInfo;
	protected ExecuteSqlContext		updateInfo;
	protected ExecuteSqlContext		deleteInfo;
	protected ExecuteSqlContext		insertInfo;
	protected EntityOperator<T>		strategyOperation;
	protected ResultSetTransfer<T>	transfer;
	protected String[]				pkName;
	protected SqlInterceptor[]		sqlInterceptors;
	protected Dialect				dialect;
	protected static final Unsafe	unsafe	= ReflectUtil.getUnsafe();
	protected static final Logger	LOGGER	= LoggerFactory.getLogger(BaseDAO.class);
	
	/**
	 * 初始化
	 */
	@Override
	public void initialize(TableMetaData<T> metaData, SqlInterceptor[] sqlInterceptors, SessionfactoryConfig config, PageParse pageParse, Dialect dialect)
	{
		this.entityClass = (Class<T>) metaData.getEntityClass();
		this.dialect = dialect;
		this.sqlInterceptors = sqlInterceptors;
		tableName = metaData.getTableName();
		setTransfer(config);
		setColumnCorrelation(metaData);
		setUpdateInfo();
		setQuery();
		setDeleteInfo();
		setInsertInfo();
		setAutoGeneratePkInsertInfo();
		strategyOperation = new EntityOperatorImpl<T>();
		strategyOperation.initialize(metaData, config, sqlInterceptors, tableName, pageParse, dialect);
	}
	
	protected abstract void setAutoGeneratePkInsertInfo();
	
	private void setInsertInfo()
	{
		StringCache cache = new StringCache();
		List<FieldValueFetcher> fetchers = new ArrayList<BaseDAO<T>.FieldValueFetcher>();
		for (MapColumn column : allColumns)
		{
			cache.append(column.getFieldName()).appendComma();
			fetchers.add(new FieldValueFetcher(column.getField()));
		}
		cache.deleteLast();
		insertInfo = new ExecuteSqlContext(cache.toString(), fetchers.toArray());
	}
	
	/**
	 * 设置列相关内容。主要是设置主键列，全部列数组，以及值列数组
	 * 
	 * @param metaData
	 */
	private void setColumnCorrelation(TableMetaData<T> metaData)
	{
		Collection<MapColumn> allColumns = new HashSet<MapColumn>(metaData.getAllColumns().values());
		this.allColumns = allColumns.toArray(new MapColumn[allColumns.size()]);
		allColumns.remove(metaData.getPkColumn());
		valueColumns = allColumns.toArray(new MapColumn[allColumns.size()]);
		pkColumn = metaData.getPkColumn();
		pkColumnOffset = unsafe.objectFieldOffset(pkColumn.getField());
		pkType = getIdType(pkColumn.getField());
		pkName = new String[] { pkColumn.getColName() };
	}
	
	private void setTransfer(SessionfactoryConfig config)
	{
		transfer = new BeanTransfer<T>();
		transfer.initialize(entityClass, config);
	}
	
	protected void setQuery()
	{
		StringCache cache = new StringCache();
		for (MapColumn each : allColumns)
		{
			cache.append(each.getFieldName()).append(",");
		}
		cache.deleteLast().append(";").append(pkColumn.getFieldName());
		queryInfo = new ExecuteSqlContext(cache.toString());
	}
	
	protected void setUpdateInfo()
	{
		StringCache cache = new StringCache();
		List<FieldValueFetcher> fetchers = new ArrayList<BaseDAO<T>.FieldValueFetcher>();
		for (MapColumn each : valueColumns)
		{
			cache.append(each.getFieldName()).appendComma();
			fetchers.add(new FieldValueFetcher(each.getField()));
		}
		cache.deleteLast().append(";").append(pkColumn.getFieldName());
		fetchers.add(new FieldValueFetcher(pkColumn.getField()));
		updateInfo = new ExecuteSqlContext(cache.toString(), fetchers.toArray());
	}
	
	protected PkType getIdType(Field field)
	{
		Class<?> type = field.getType();
		if (type == String.class)
		{
			return PkType.STRING;
		}
		else if (type == Integer.class)
		{
			return PkType.INT;
		}
		else if (type == Long.class)
		{
			return PkType.LONG;
		}
		else
		{
			throw new UnsupportedOperationException("id字段只支持Integer，Long，String");
		}
		
	}
	
	protected void setDeleteInfo()
	{
		deleteInfo = new ExecuteSqlContext(pkColumn.getFieldName(), new FieldValueFetcher(pkColumn.getField()));
	}
	
	@Override
	public int delete(Object entity, Connection connection)
	{
		return strategyOperation.delete(connection, deleteInfo.sql, deleteInfo.parseParams(entity));
	}
	
	@Override
	public T getById(Object pk, Connection connection)
	{
		return strategyOperation.findOne(connection, queryInfo.sql, pk);
	}
	
	@Override
	public void insert(Object entity, Connection connection)
	{
		strategyOperation.insert(connection, insertInfo.sql, insertInfo.parseParams(entity));
	}
	
	@Override
	public void save(Object entity, Connection connection)
	{
		Object idValue = unsafe.getObject(entity, pkColumnOffset);
		if (idValue == null)
		{
			autoGeneratePkInsert(entity, connection);
		}
		else
		{
			strategyOperation.update(connection, updateInfo.sql, updateInfo.parseParams(entity));
		}
		
	}
	
	/**
	 * 自动生成主键方式新增数据
	 * 
	 * @param entity
	 * @param connection
	 */
	protected abstract void autoGeneratePkInsert(Object entity, Connection connection);
	
	@Override
	public int update(Object entity, Connection connection)
	{
		return strategyOperation.update(connection, updateInfo.sql, updateInfo.parseParams(entity));
	}
	
	@Override
	public T getById(Object pk, Connection connection, LockMode mode)
	{
		switch (mode)
		{
			case SHARE:
				return strategyOperation.findOneForShare(connection, queryInfo.sql, pk);
			case UPDATE:
				return strategyOperation.findOneForUpdate(connection, queryInfo.sql, pk);
			default:
				throw new NullPointerException();
		}
	}
	
	@Override
	public int deleteAll(Connection connection)
	{
		return ExecSqlTemplate.update(dialect, sqlInterceptors, connection, "DELETE FROM " + tableName);
	}
	
	@Override
	public int update(Connection connection, String strategy, Object... params)
	{
		return strategyOperation.update(connection, strategy, params);
	}
	
	@Override
	public int delete(Connection connection, String strategy, Object... params)
	{
		return strategyOperation.delete(connection, strategy, params);
	}
	
	@Override
	public T findOne(Connection connection, String strategy, Object... params)
	{
		return strategyOperation.findOne(connection, strategy, params);
	}
	
	@Override
	public List<T> findAll(Connection connection, String strategy, Object... params)
	{
		return strategyOperation.findAll(connection, strategy, params);
	}
	
	@Override
	public List<T> findPage(Connection connection, Page page, String strategy, Object... params)
	{
		return strategyOperation.findPage(connection, page, strategy, params);
	}
	
	@Override
	public int count(Connection connection, String strategy, Object... params)
	{
		return strategyOperation.count(connection, strategy, params);
	}
	
	@Override
	public int insert(Connection connection, String strategy, Object... params)
	{
		return strategyOperation.insert(connection, strategy, params);
	}
	
	protected Object[] parseParams(Object entity, FieldValueFetcher[] fetchers)
	{
		Object[] params = new Object[fetchers.length];
		for (int i = 0; i < params.length; i++)
		{
			params[i] = fetchers[i].fieldValue(entity);
		}
		return params;
	}
	
}
