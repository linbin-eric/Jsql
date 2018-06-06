package com.jfireframework.sql.curd.impl;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.pkstrategy.Sequence;
import com.jfireframework.sql.util.TableEntityInfo;

public class OracleCurdInfo<T> extends AbstractCurdInfo<T>
{
	
	public OracleCurdInfo(Class<T> ckass)
	{
		super(ckass);
	}
	
	@Override
	protected void generateNative(Class<T> ckass, TableEntityInfo tableEntityInfo, Map<String, String> propertyNameToColumnNameMap, Field pkField)
	{
		if (Number.class.isAssignableFrom(pkField.getType()) && pkField.isAnnotationPresent(Sequence.class))
		{
			List<Field> list = new LinkedList<Field>();
			StringCache cache = new StringCache();
			cache.append("insert into ").append(tableEntityInfo.getTableName()).append("(");
			cache.append(propertyNameToColumnNameMap.get(pkField.getName())).appendComma();
			for (Field field : ReflectUtil.getAllFields(ckass))
			{
				if (field.equals(pkField))
				{
					continue;
				}
				cache.append(propertyNameToColumnNameMap.get(field.getName())).appendComma();
				list.add(field);
			}
			cache.deleteLast().append(") values (").append(pkField.getAnnotation(Sequence.class).value()).append(".NEXTVAL,");
			int size = list.size();
			for (int i = 0; i < size; i++)
			{
				cache.append("?,");
			}
			cache.deleteLast().append(")");
		}
		
	}
	
}
