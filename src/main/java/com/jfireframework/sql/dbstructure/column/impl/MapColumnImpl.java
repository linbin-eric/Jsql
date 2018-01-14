package com.jfireframework.sql.dbstructure.column.impl;

import java.lang.reflect.Field;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.sql.annotation.UserDefinedColumnType;
import com.jfireframework.sql.dbstructure.column.ColumnType;
import com.jfireframework.sql.dbstructure.column.ColumnTypeDictionary;
import com.jfireframework.sql.dbstructure.column.MapColumn;
import com.jfireframework.sql.dbstructure.name.ColumnNameStrategy;
import com.jfireframework.sql.util.CommonHelper;

/**
 * 基础CURD操作映射的抽象属性类
 * 
 * @author linbin
 * 
 */
public class MapColumnImpl implements MapColumn
{
	protected String		dbColName;
	protected Field			field;
	protected ColumnType	columnType;
	
	@Override
	public void initialize(Field field, ColumnNameStrategy colNameStrategy, ColumnTypeDictionary columnTypeDictionary)
	{
		this.field = field;
		dbColName = CommonHelper.columnName(field, colNameStrategy);
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
	public ColumnType getColumnType()
	{
		return columnType;
	}
	
}
