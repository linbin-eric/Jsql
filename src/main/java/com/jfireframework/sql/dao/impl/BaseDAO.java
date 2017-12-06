package com.jfireframework.sql.dao.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.annotation.Pk;
import com.jfireframework.sql.dao.Dao;
import com.jfireframework.sql.dao.LockMode;
import com.jfireframework.sql.dao.StrategyOperation;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;
import com.jfireframework.sql.resultsettransfer.impl.BeanTransfer;
import com.jfireframework.sql.session.ExecSqlTemplate;
import com.jfireframework.sql.util.ExecuteSqlInfo;
import com.jfireframework.sql.util.PkType;
import sun.misc.Unsafe;

public abstract class BaseDAO<T> implements Dao<T>
{
	
	protected Class<T>				entityClass;
	// 数据表主键的列
	protected MapField				pkColumn;
	// 数据表的所有列
	protected MapField[]			allColumns;
	// 数据表除了主键之外所有列
	protected MapField[]			valueColumns;
	protected long					pkColumnOffset;
	protected PkType				pkType;
	protected String				tableName;
	protected ExecuteSqlInfo		queryInfo;
	protected ExecuteSqlInfo		queryInShareInfo;
	protected ExecuteSqlInfo		queryForUpdateInfo;
	protected ExecuteSqlInfo		updateInfo;
	protected ExecuteSqlInfo		deleteInfo;
	protected ExecuteSqlInfo		insertInfo;
	protected StrategyOperation<T>	strategyOperation;
	protected ResultSetTransfer<T>	transfer;
	protected String[]				pkName;
	protected SqlInterceptor[]		sqlInterceptors;
	protected Dialect				dialect;
	protected static final Unsafe	unsafe	= ReflectUtil.getUnsafe();
	protected static final Logger	LOGGER	= LoggerFactory.getLogger(BaseDAO.class);
	
	/**
	 * 初始化
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(TableMetaData metaData, SqlInterceptor[] sqlInterceptors, SessionfactoryConfig config, PageParse pageParse, Dialect dialect)
	{
		this.entityClass = (Class<T>) metaData.getEntityClass();
		this.dialect = dialect;
		this.sqlInterceptors = sqlInterceptors;
		tableName = metaData.getTableName();
		setTransfer(config);
		setColumnCorrelation(metaData);
		setUpdateInfo();
		setQuery();
		setQueryForUpdate();
		setQueryInShare();
		setDeleteInfo();
		setInsertInfo();
		setAutoGeneratePkInsertInfo();
		strategyOperation = new StrategyOperationImpl<T>(entityClass, allColumns, config, sqlInterceptors, tableName, pageParse, dialect);
	}
	
	protected abstract void setAutoGeneratePkInsertInfo();
	
	private void setInsertInfo()
	{
		StringCache cache = new StringCache();
		cache.append("INSERT INTO ").append(tableName).append('(');
		for (MapField column : allColumns)
		{
			cache.append(column.getColName()).appendComma();
		}
		cache.deleteLast().append(") VALUES(");
		for (int i = 0; i < allColumns.length; i++)
		{
			cache.append("?").appendComma();
		}
		cache.deleteLast().append(")");
		insertInfo = new ExecuteSqlInfo(cache.toString(), allColumns);
	}
	
	/**
	 * 设置列相关内容。主要是设置主键列，全部列数组，以及值列数组
	 * 
	 * @param metaData
	 */
	private void setColumnCorrelation(TableMetaData metaData)
	{
		allColumns = metaData.getFieldInfos();
		List<MapField> columns = new ArrayList<MapField>();
		for (MapField column : allColumns)
		{
			if (column.getField().isAnnotationPresent(Pk.class))
			{
				pkColumn = column;
				pkColumnOffset = unsafe.objectFieldOffset(pkColumn.getField());
				pkType = getIdType(pkColumn.getField());
				pkName = new String[] { pkColumn.getColName() };
			}
			else
			{
				columns.add(column);
			}
		}
		valueColumns = columns.toArray(new MapField[columns.size()]);
	}
	
	private void setTransfer(SessionfactoryConfig config)
	{
		transfer = new BeanTransfer<T>();
		transfer.initialize(entityClass, config);
	}
	
	protected void setQuery()
	{
		StringCache cache = new StringCache();
		cache.append("select ");
		for (MapField each : allColumns)
		{
			cache.append(each.getColName()).append(",");
		}
		cache.deleteLast().append(" from ").append(tableName).append(" where ").append(pkColumn.getColName()).append("=?");
		queryInfo = new ExecuteSqlInfo(cache.toString(), new MapField[] { pkColumn });
	}
	
	protected void setUpdateInfo()
	{
		List<MapField> params = new LinkedList<MapField>();
		StringCache cache = new StringCache();
		cache.append("update ").append(tableName).append(" set ");
		for (MapField each : valueColumns)
		{
			params.add(each);
			cache.append(each.getColName()).append("=?,");
		}
		cache.deleteLast().append(" where ").append(pkColumn.getColName()).append("=?");
		params.add(pkColumn);
		updateInfo = new ExecuteSqlInfo(cache.toString(), params.toArray(new MapField[params.size()]));
	}
	
	protected void setQueryForUpdate()
	{
		StringCache cache = new StringCache();
		cache.append("select ");
		for (MapField each : allColumns)
		{
			cache.append(each.getColName()).append(",");
		}
		cache.deleteLast().append(" from ").append(tableName).append(" where ").append(pkColumn.getColName()).append("=? FOR UPDATE");
		queryForUpdateInfo = new ExecuteSqlInfo(cache.toString(), new MapField[] { pkColumn });
	}
	
	protected void setQueryInShare()
	{
		StringCache cache = new StringCache();
		cache.append("select ");
		for (MapField each : allColumns)
		{
			cache.append(each.getColName()).append(",");
		}
		cache.deleteLast().append(" from ").append(tableName).append(" where ").append(pkColumn.getColName()).append("=? LOCK IN SHARE MODE");
		queryInShareInfo = new ExecuteSqlInfo(cache.toString(), new MapField[] { pkColumn });
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
		String sql = StringUtil.format("DELETE FROM {} WHERE {}=?", tableName, pkColumn.getColName());
		deleteInfo = new ExecuteSqlInfo(sql, new MapField[] { pkColumn });
	}
	
	@Override
	public int delete(Object entity, Connection connection)
	{
		return ExecSqlTemplate.update(dialect, sqlInterceptors, connection, deleteInfo.getSql(), parseParam(deleteInfo.getColumns(), entity));
	}
	
	@Override
	public T getById(Object pk, Connection connection)
	{
		return ExecSqlTemplate.queryOne(dialect, sqlInterceptors, transfer, connection, queryInfo.getSql(), pk);
	}
	
	@Override
	public void insert(Object entity, Connection connection)
	{
		ExecSqlTemplate.insert(dialect, sqlInterceptors, connection, insertInfo.getSql(), parseParam(insertInfo.getColumns(), entity));
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
			ExecSqlTemplate.update(dialect, sqlInterceptors, connection, updateInfo.getSql(), parseParam(updateInfo.getColumns(), entity));
		}
		
	}
	
	/**
	 * 自动生成主键方式新增数据
	 * 
	 * @param entity
	 * @param connection
	 */
	protected abstract void autoGeneratePkInsert(Object entity, Connection connection);
	
	protected Object[] parseParam(MapField[] fields, Object entity)
	{
		Object[] params = new Object[fields.length];
		for (int i = 0; i < params.length; i++)
		{
			params[i] = fields[i].fieldValue(entity);
		}
		return params;
	}
	
	@Override
	public int update(Object entity, Connection connection)
	{
		return ExecSqlTemplate.update(dialect, sqlInterceptors, connection, updateInfo.getSql(), parseParam(updateInfo.getColumns(), entity));
	}
	
	@Override
	public T getById(Object pk, Connection connection, LockMode mode)
	{
		String sql = mode == LockMode.SHARE ? queryInShareInfo.getSql() : queryForUpdateInfo.getSql();
		return ExecSqlTemplate.queryOne(dialect, sqlInterceptors, transfer, connection, sql, pk);
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
		// TODO Auto-generated method stub
		return 0;
	}
	
}
