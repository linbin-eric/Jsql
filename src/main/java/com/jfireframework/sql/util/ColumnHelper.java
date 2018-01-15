package com.jfireframework.sql.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Blob;
import java.sql.Clob;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.annotation.Column;
import com.jfireframework.sql.annotation.NameStrategy;
import com.jfireframework.sql.annotation.SqlIgnore;
import com.jfireframework.sql.annotation.UserDefinedColumnTransfer;
import com.jfireframework.sql.dbstructure.name.ColumnNameStrategy;
import com.jfireframework.sql.dbstructure.name.DefaultNameStrategy;
import com.jfireframework.sql.transfer.column.ColumnTransfer;
import com.jfireframework.sql.transfer.column.ColumnTransferDictionary;

public class ColumnHelper
{
	
	public static ColumnTransfer getColumnTransfer(Field field, ColumnTransferDictionary dictionary)
	{
		Class<? extends ColumnTransfer> operatorType = null;
		if (field.isAnnotationPresent(UserDefinedColumnTransfer.class))
		{
			try
			{
				operatorType = field.getAnnotation(UserDefinedColumnTransfer.class).value();
			}
			catch (Exception e)
			{
				throw new JustThrowException(e);
			}
		}
		if (operatorType == null)
		{
			operatorType = dictionary.dictionary(field);
		}
		if (operatorType != null)
		{
			ColumnTransfer operator;
			try
			{
				operator = operatorType.newInstance();
				operator.initialize(field);
				return operator;
			}
			catch (Exception e)
			{
				throw new JustThrowException(e);
			}
		}
		else
		{
			throw new NullPointerException(StringUtil.format("属性{}.{}的类型尚未支持", field.getDeclaringClass(), field.getName()));
		}
	}
	
	public static boolean notColumnField(Field field)
	{
		if (field.getType() == Clob.class || field.getType() == Blob.class)
		{
			return false;
		}
		if (field.isAnnotationPresent(SqlIgnore.class) //
		        || Map.class.isAssignableFrom(field.getType())//
		        || List.class.isAssignableFrom(field.getType())//
		        || (field.getType().isInterface())//
		        || Modifier.isStatic(field.getModifiers()))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static ColumnNameStrategy columnNameStrategy(Class<?> ckass)
	{
		try
		{
			Class<? extends ColumnNameStrategy> columnNameStrategyClass = ckass.isAnnotationPresent(NameStrategy.class) ? ckass.getAnnotation(NameStrategy.class).value() : DefaultNameStrategy.class;
			return columnNameStrategyClass.newInstance();
		}
		catch (Exception e)
		{
			throw new JustThrowException(e);
		}
	}
	
	public static String columnName(Field field, ColumnNameStrategy columnNameStrategy)
	{
		if (field.isAnnotationPresent(Column.class))
		{
			Column column = field.getAnnotation(Column.class);
			if (StringUtil.isNotBlank(column.name()))
			{
				return field.getAnnotation(Column.class).name();
			}
			else
			{
				return columnNameStrategy.toDbName(field.getName());
			}
		}
		else
		{
			return columnNameStrategy.toDbName(field.getName());
		}
	}
}
