package com.jfireframework.sql.curd.impl;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.pkstrategy.AutoIncrement;
import com.jfireframework.sql.util.TableEntityInfo;

public class MysqlCurdInfo<T> extends AbstractCurdInfo<T>
{
	
	public MysqlCurdInfo(Class<T> ckass)
	{
		super(ckass);
	}
	
	@Override
	protected void generateNative(Class<T> ckass, TableEntityInfo tableEntityInfo, Map<String, String> propertyNameToColumnNameMap, Field pkField)
	{
		if (Number.class.isAssignableFrom(pkField.getType()) || pkField.isAnnotationPresent(AutoIncrement.class))
		{
			StringCache cache = new StringCache();
			cache.append("insert into ").append(tableEntityInfo.getTableName()).append(" (");
			List<Field> list = new LinkedList<Field>();
			for (Field field : ReflectUtil.getAllFields(ckass))
			{
				if (field.equals(pkField))
				{
					continue;
				}
				cache.append(propertyNameToColumnNameMap.get(field.getName())).append(",");
				list.add(field);
			}
			cache.deleteLast().append(") values (");
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
	}
	
}
