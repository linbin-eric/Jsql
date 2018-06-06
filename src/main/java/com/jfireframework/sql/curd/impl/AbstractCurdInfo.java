package com.jfireframework.sql.curd.impl;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.pkstrategy.PkGenerator;
import com.jfireframework.sql.curd.CurdInfo;
import com.jfireframework.sql.curd.LockMode;
import com.jfireframework.sql.util.TableEntityInfo;

public abstract class AbstractCurdInfo implements CurdInfo
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
	
	public AbstractCurdInfo(Class<?> ckass)
	{
		TableEntityInfo tableEntityInfo = TableEntityInfo.parse(ckass);
		Map<String, String> propertyNameToColumnNameMap = tableEntityInfo.getPropertyNameToColumnNameMap();
		generateInsertEntry(ckass, tableEntityInfo, propertyNameToColumnNameMap);
		generateDeleteEntry(tableEntityInfo, propertyNameToColumnNameMap);
		generateUpadteEntry(ckass, tableEntityInfo, propertyNameToColumnNameMap);
		generateGetEntry(tableEntityInfo, propertyNameToColumnNameMap);
		generateLockInShareEntry(tableEntityInfo, propertyNameToColumnNameMap);
		generateLockForUpdateEntry(tableEntityInfo, propertyNameToColumnNameMap);
		if (tableEntityInfo.getPkField().isAnnotationPresent(PkGenerator.class))
		{
			generatePkGenerator(ckass, tableEntityInfo, propertyNameToColumnNameMap, tableEntityInfo.getPkField());
		}
		else
		{
			generateNative(ckass, tableEntityInfo, propertyNameToColumnNameMap, tableEntityInfo.getPkField());
		}
	}
	
	protected abstract void generateNative(Class<?> ckass, TableEntityInfo tableEntityInfo, Map<String, String> propertyNameToColumnNameMap, Field pkField);
	
	private void generatePkGenerator(Class<?> ckass, TableEntityInfo tableEntityInfo, Map<String, String> propertyNameToColumnNameMap, Field pkField)
	{
		try
		{
			generator = pkField.getAnnotation(PkGenerator.class).value().newInstance();
			StringCache cache = new StringCache();
			List<Field> list = new LinkedList<Field>();
			cache.append("insert into ").append(tableEntityInfo.getTableName()).append(" (").append(propertyNameToColumnNameMap.get(pkField.getName())).appendComma();
			for (Field field : ReflectUtil.getAllFields(ckass))
			{
				if (field.equals(pkField))
				{
					continue;
				}
				cache.append(propertyNameToColumnNameMap.get(field.getName())).appendComma();
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
			throw new JustThrowException(e);
		}
	}
	
	private void generateLockForUpdateEntry(TableEntityInfo tableEntityInfo, Map<String, String> propertyNameToColumnNameMap)
	{
		StringCache cache = new StringCache();
		cache.append("select * from ").append(tableEntityInfo.getTableName()).append(" where ").append(propertyNameToColumnNameMap.get(tableEntityInfo.getPkField().getName())).append("=? for update");
		lockForUpdateEntry = new SqlAndFieldEntry();
		lockForUpdateEntry.sql = cache.toString();
		lockForUpdateEntry.fields = new Field[] { tableEntityInfo.getPkField() };
	}
	
	private void generateLockInShareEntry(TableEntityInfo tableEntityInfo, Map<String, String> propertyNameToColumnNameMap)
	{
		StringCache cache = new StringCache();
		cache.append("select * from ").append(tableEntityInfo.getTableName()).append(" where ").append(propertyNameToColumnNameMap.get(tableEntityInfo.getPkField().getName())).append("=? lock in share mode");
		lockInShareEntry = new SqlAndFieldEntry();
		lockInShareEntry.sql = cache.toString();
		lockInShareEntry.fields = new Field[] { tableEntityInfo.getPkField() };
	}
	
	private void generateGetEntry(TableEntityInfo tableEntityInfo, Map<String, String> propertyNameToColumnNameMap)
	{
		StringCache cache = new StringCache();
		cache.append("select * from ").append(tableEntityInfo.getTableName()).append(" where ").append(propertyNameToColumnNameMap.get(tableEntityInfo.getPkField().getName())).append("=?");
		getEntry = new SqlAndFieldEntry();
		getEntry.sql = cache.toString();
		getEntry.fields = new Field[] { tableEntityInfo.getPkField() };
	}
	
	private void generateUpadteEntry(Class<?> ckass, TableEntityInfo tableEntityInfo, Map<String, String> propertyNameToColumnNameMap)
	{
		StringCache cache = new StringCache();
		cache.append("update ").append(tableEntityInfo.getTableName()).append(" set ");
		List<Field> list = new LinkedList<Field>();
		for (Field each : ReflectUtil.getAllFields(ckass))
		{
			cache.append(propertyNameToColumnNameMap.get(each.getName())).append("=?,");
			each.setAccessible(true);
			list.add(each);
		}
		cache.deleteLast().append(") where ").append(propertyNameToColumnNameMap.get(tableEntityInfo.getPkField().getName())).append("=?");
		list.add(tableEntityInfo.getPkField());
		updateEntry.sql = cache.toString();
		updateEntry.fields = list.toArray(new Field[0]);
	}
	
	private void generateDeleteEntry(TableEntityInfo tableEntityInfo, Map<String, String> propertyNameToColumnNameMap)
	{
		StringCache cache = new StringCache();
		cache.append("delete from ").append(tableEntityInfo.getTableName()).append(" where ").append(propertyNameToColumnNameMap.get(tableEntityInfo.getPkField().getName()))//
		        .append("=?");
		deleteEntry = new SqlAndFieldEntry();
		deleteEntry.sql = cache.toString();
		deleteEntry.fields = new Field[] { tableEntityInfo.getPkField() };
	}
	
	private void generateInsertEntry(Class<?> ckass, TableEntityInfo tableEntityInfo, Map<String, String> propertyNameToColumnNameMap)
	{
		StringCache cache = new StringCache();
		List<Field> list = new LinkedList<Field>();
		cache.append("insert into ").append(tableEntityInfo.getTableName()).append(" (");
		for (Field each : ReflectUtil.getAllFields(ckass))
		{
			if (propertyNameToColumnNameMap.containsKey(each.getName()))
			{
				cache.append(propertyNameToColumnNameMap.get(each.getName())).appendComma();
				each.setAccessible(true);
				list.add(each);
			}
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
	public String insert(Object entity, List<Object> params)
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
			throw new JustThrowException(e);
		}
	}
	
	@Override
	public String update(Object entity, List<Object> params)
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
			throw new JustThrowException(e);
		}
	}
	
	@Override
	public String find(Class<?> ckass, Object pk, List<Object> params)
	{
		params.add(pk);
		return getEntry.sql;
	}
	
	@Override
	public String find(Class<?> ckass, Object pk, LockMode mode, List<Object> params)
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
	public String autoGeneratePkInsert(Object entity, List<Object> params)
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
			throw new JustThrowException(e);
		}
	}
}
