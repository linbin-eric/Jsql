package com.jfireframework.sql.curd.impl;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.annotation.pkstrategy.PkGenerator;
import com.jfireframework.sql.curd.CurdInfo;
import com.jfireframework.sql.curd.LockMode;
import com.jfireframework.sql.metadata.TableEntityInfo;
import com.jfireframework.sql.metadata.TableEntityInfo.ColumnInfo;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;
import com.jfireframework.sql.transfer.resultset.impl.BeanTransfer;

public abstract class AbstractCurdInfo<T> implements CurdInfo<T>
{
	class SqlAndFieldEntry
	{
		String	sql;
		Field[]	fields;
	}
	
	protected SqlAndFieldEntry		insertEntry;
	protected SqlAndFieldEntry		deleteEntry;
	protected SqlAndFieldEntry		updateEntry;
	protected SqlAndFieldEntry		getEntry;
	protected SqlAndFieldEntry		lockInShareEntry;
	protected SqlAndFieldEntry		lockForUpdateEntry;
	protected SqlAndFieldEntry		autoGeneratePkInsertEntry;
	protected PkGenerator.Generator	generator;
	private PkMode					mode	= PkMode.OTHER;
	private ResultSetTransfer		beanTransfer;
	private Field					pkField;
	
	enum PkMode
	{
		STRING, INT, LONG, OTHER
	}
	
	public AbstractCurdInfo(Class<T> ckass)
	{
		TableEntityInfo tableEntityInfo = TableEntityInfo.parse(ckass);
		generateInsertEntry(ckass, tableEntityInfo);
		generateDeleteEntry(tableEntityInfo);
		generateUpadteEntry(ckass, tableEntityInfo);
		generateGetEntry(tableEntityInfo);
		generateLockInShareEntry(tableEntityInfo);
		generateLockForUpdateEntry(tableEntityInfo);
		if (tableEntityInfo.getPkInfo().getField().isAnnotationPresent(PkGenerator.class))
		{
			generatePkGenerator(tableEntityInfo);
		}
		else
		{
			generateNative(tableEntityInfo);
		}
		beanTransfer = new BeanTransfer().initialize(ckass);
		pkField = tableEntityInfo.getPkInfo().getField();
		if (pkField.getType() == String.class)
		{
			mode = PkMode.STRING;
		}
		else if (pkField.getType() == Integer.class)
		{
			mode = PkMode.INT;
		}
		else if (pkField.getGenericType() == Long.class)
		{
			mode = PkMode.LONG;
		}
	}
	
	protected abstract void generateNative(TableEntityInfo tableEntityInfo);
	
	private void generatePkGenerator(TableEntityInfo tableEntityInfo)
	{
		try
		{
			Field pkField = tableEntityInfo.getPkInfo().getField();
			generator = pkField.getAnnotation(PkGenerator.class).value().newInstance();
			StringCache cache = new StringCache();
			List<Field> list = new LinkedList<Field>();
			cache.append("insert into ").append(tableEntityInfo.getTableName()).append(" (").append(tableEntityInfo.getPkInfo().getColumnName()).appendComma();
			for (ColumnInfo info : tableEntityInfo.getPropertyNameKeyMap().values())
			{
				Field field = info.getField();
				if (field.equals(pkField))
				{
					continue;
				}
				cache.append(info.getColumnName()).appendComma();
				list.add(field);
			}
			cache.deleteLast().append(") values (?,");
			int size = list.size();
			for (int i = 0; i < size; i++)
			{
				cache.append("?,");
			}
			cache.deleteLast().append(")");
			autoGeneratePkInsertEntry = new SqlAndFieldEntry();
			autoGeneratePkInsertEntry.sql = cache.toString();
			autoGeneratePkInsertEntry.fields = list.toArray(new Field[0]);
		}
		catch (Exception e)
		{
			ReflectUtil.throwException(e);
		}
	}
	
	private void generateLockForUpdateEntry(TableEntityInfo tableEntityInfo)
	{
		StringCache cache = new StringCache();
		cache.append("select * from ").append(tableEntityInfo.getTableName()).append(" where ").append(tableEntityInfo.getPkInfo().getColumnName()).append("=? for update");
		lockForUpdateEntry = new SqlAndFieldEntry();
		lockForUpdateEntry.sql = cache.toString();
		lockForUpdateEntry.fields = new Field[] { tableEntityInfo.getPkInfo().getField() };
	}
	
	private void generateLockInShareEntry(TableEntityInfo tableEntityInfo)
	{
		StringCache cache = new StringCache();
		cache.append("select * from ").append(tableEntityInfo.getTableName()).append(" where ").append(tableEntityInfo.getPkInfo().getField()).append("=? lock in share mode");
		lockInShareEntry = new SqlAndFieldEntry();
		lockInShareEntry.sql = cache.toString();
		lockInShareEntry.fields = new Field[] { tableEntityInfo.getPkInfo().getField() };
	}
	
	private void generateGetEntry(TableEntityInfo tableEntityInfo)
	{
		StringCache cache = new StringCache();
		cache.append("select  ");
		for (ColumnInfo info : tableEntityInfo.getPropertyNameKeyMap().values())
		{
			cache.append(info.getColumnName()).appendComma();
		}
		cache.deleteLast().append(" from ");
		cache.append(tableEntityInfo.getTableName()).append(" where ").append(tableEntityInfo.getPkInfo().getColumnName()).append("=?");
		getEntry = new SqlAndFieldEntry();
		getEntry.sql = cache.toString();
		getEntry.fields = new Field[] { tableEntityInfo.getPkInfo().getField() };
	}
	
	private void generateUpadteEntry(Class<?> ckass, TableEntityInfo tableEntityInfo)
	{
		StringCache cache = new StringCache();
		cache.append("update ").append(tableEntityInfo.getTableName()).append(" set ");
		List<Field> list = new LinkedList<Field>();
		for (ColumnInfo info : tableEntityInfo.getPropertyNameKeyMap().values())
		{
			cache.append(info.getColumnName()).append("=?,");
			list.add(info.getField());
		}
		cache.deleteLast().append(" where ").append(tableEntityInfo.getPkInfo().getColumnName()).append("=?");
		list.add(tableEntityInfo.getPkInfo().getField());
		updateEntry = new SqlAndFieldEntry();
		updateEntry.sql = cache.toString();
		updateEntry.fields = list.toArray(new Field[0]);
	}
	
	private void generateDeleteEntry(TableEntityInfo tableEntityInfo)
	{
		StringCache cache = new StringCache();
		cache.append("delete from ").append(tableEntityInfo.getTableName()).append(" where ").append(tableEntityInfo.getPkInfo().getColumnName())//
		        .append("=?");
		deleteEntry = new SqlAndFieldEntry();
		deleteEntry.sql = cache.toString();
		deleteEntry.fields = new Field[] { tableEntityInfo.getPkInfo().getField() };
	}
	
	private void generateInsertEntry(Class<?> ckass, TableEntityInfo tableEntityInfo)
	{
		StringCache cache = new StringCache();
		List<Field> list = new LinkedList<Field>();
		cache.append("insert into ").append(tableEntityInfo.getTableName()).append(" (");
		for (ColumnInfo columnInfo : tableEntityInfo.getPropertyNameKeyMap().values())
		{
			cache.append(columnInfo.getColumnName()).appendComma();
			list.add(columnInfo.getField());
		}
		cache.deleteLast().append(") values(");
		int size = list.size();
		for (int i = 0; i < size; i++)
		{
			cache.append("?,");
		}
		cache.deleteLast().append(')');
		insertEntry = new SqlAndFieldEntry();
		insertEntry.sql = cache.toString();
		insertEntry.fields = list.toArray(new Field[0]);
	}
	
	@Override
	public String insert(T entity, List<Object> params)
	{
		try
		{
			for (Field field : insertEntry.fields)
			{
				params.add(field.get(entity));
			}
			return insertEntry.sql;
		}
		catch (Exception e)
		{
			ReflectUtil.throwException(e);
			return null;
		}
	}
	
	@Override
	public String update(T entity, List<Object> params)
	{
		try
		{
			for (Field field : updateEntry.fields)
			{
				params.add(field.get(entity));
			}
			return updateEntry.sql;
		}
		catch (Exception e)
		{
			ReflectUtil.throwException(e);
			return null;
		}
	}
	
	@Override
	public String find(Object pk, List<Object> params)
	{
		params.add(pk);
		return getEntry.sql;
	}
	
	@Override
	public String delete(Object pk, List<Object> params)
	{
		params.add(pk);
		return deleteEntry.sql;
	}
	
	@Override
	public String find(Object pk, LockMode mode, List<Object> params)
	{
		params.add(pk);
		if (mode == LockMode.SHARE)
		{
			return lockInShareEntry.sql;
		}
		else if (mode == LockMode.UPDATE)
		{
			return lockForUpdateEntry.sql;
		}
		else
		{
			throw new NullPointerException();
		}
	}
	
	@Override
	public String autoGeneratePkInsert(T entity, List<Object> params)
	{
		try
		{
			if (generator != null)
			{
				params.add(generator.next());
			}
			for (Field field : autoGeneratePkInsertEntry.fields)
			{
				params.add(field.get(entity));
			}
			return autoGeneratePkInsertEntry.sql;
		}
		catch (Exception e)
		{
			ReflectUtil.throwException(e);
			return null;
		}
	}
	
	@Override
	public void setPkValue(T entity, String pk)
	{
		try
		{
			switch (mode)
			{
				case INT:
					pkField.set(entity, Integer.valueOf(pk));
					break;
				case STRING:
					pkField.set(entity, pk);
					break;
				case LONG:
					pkField.set(entity, Long.valueOf(pk));
					break;
				case OTHER:
					break;
				default:
					break;
			}
		}
		catch (Exception e)
		{
			ReflectUtil.throwException(e);
		}
	}
	
	@Override
	public ResultSetTransfer getBeanTransfer()
	{
		return beanTransfer;
	}
	
	public void setSessionFactory(SessionFactory sessionFactory)
	{
		if (generator != null)
		{
			generator.setSessionFactory(sessionFactory);
		}
	}
}
