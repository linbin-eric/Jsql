package com.jfireframework.sql.mapfield.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.Column;
import com.jfireframework.sql.dbstructure.column.ColumnType;
import com.jfireframework.sql.dbstructure.column.ColumnTypeDictionary;
import com.jfireframework.sql.dbstructure.column.UserDefinedColumnType;
import com.jfireframework.sql.dbstructure.name.ColumnNameStrategy;
import com.jfireframework.sql.mapfield.FieldOperator;
import com.jfireframework.sql.mapfield.FieldOperatorDictionary;
import com.jfireframework.sql.mapfield.FieldOperatorUtil;
import com.jfireframework.sql.mapfield.MapField;
import sun.misc.Unsafe;

/**
 * 基础CURD操作映射的抽象属性类
 * 
 * @author linbin
 * 
 */
public class MapFieldImpl implements MapField
{
	protected final static Unsafe	unsafe	= ReflectUtil.getUnsafe();
	protected long					offset;
	protected String				dbColName;
	protected Field					field;
	protected ColumnType			columnType;
	protected FieldOperator			operator;
	
	@Override
	public void initialize(Field field, ColumnNameStrategy colNameStrategy, FieldOperatorDictionary fieldOperatorDictionary, ColumnTypeDictionary columnTypeDictionary)
	{
		offset = unsafe.objectFieldOffset(field);
		operator = FieldOperatorUtil.getFieldOperator(field, fieldOperatorDictionary);
		this.field = field;
		if (field.isAnnotationPresent(Column.class))
		{
			Column column = field.getAnnotation(Column.class);
			if (StringUtil.isNotBlank(column.name()))
			{
				dbColName = field.getAnnotation(Column.class).name();
			}
			else
			{
				dbColName = colNameStrategy.toDbName(field.getName());
			}
		}
		else
		{
			dbColName = colNameStrategy.toDbName(field.getName());
		}
		columnType = buildColumnType(field, columnTypeDictionary);
	}
	
	ColumnType buildColumnType(Field field, ColumnTypeDictionary jdbcTypeDictionary)
	{
		ColumnType columnType;
		if (field.isAnnotationPresent(UserDefinedColumnType.class))
		{
			final UserDefinedColumnType columnDesc = field.getAnnotation(UserDefinedColumnType.class);
			columnType = new ColumnType() {
				
				@Override
				public String type()
				{
					return columnDesc.type().toUpperCase();
				}
				
				@Override
				public String desc()
				{
					return columnDesc.desc().toUpperCase();
				}
			};
		}
		else
		{
			if (jdbcTypeDictionary.map(field.getType()) == null)
			{
				if (Enum.class.isAssignableFrom(field.getType()))
				{
					columnType = jdbcTypeDictionary.map(String.class);
				}
				else
				{
					throw new NullPointerException(StringUtil.format("字段:{}无法找到对应的sql映射。请进行自定义", field.getDeclaringClass().getName() + "." + field.getName()));
				}
			}
			else
			{
				columnType = jdbcTypeDictionary.map(field.getType());
			}
		}
		return columnType;
	}
	
	@Override
	public String getColName()
	{
		return dbColName;
	}
	
	@Override
	public String getFieldName()
	{
		return field.getName();
	}
	
	/**
	 * 返回原始的field对象
	 * 
	 * @return
	 */
	@Override
	public Field getField()
	{
		return field;
	}
	
	@Override
	public FieldOperator fieldOperator()
	{
		return operator;
	}
	
	@Override
	public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException
	{
		operator.setEntityValue(entity, dbColName, resultSet);
	}
	
	@Override
	public Object fieldValue(Object entity)
	{
		return operator.fieldValue(entity);
	}
	
	@Override
	public ColumnType getColumnType()
	{
		return columnType;
	}
	
}
