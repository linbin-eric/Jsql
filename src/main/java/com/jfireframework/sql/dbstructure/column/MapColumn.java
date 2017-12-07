package com.jfireframework.sql.dbstructure.column;

import java.lang.reflect.Field;
import com.jfireframework.sql.dbstructure.name.ColumnNameStrategy;

public interface MapColumn
{
	
	void initialize(Field field, ColumnNameStrategy colNameStrategy, ColumnTypeDictionary columnTypeDictionary);
	
	/**
	 * 获取该属性所对应的数据库字段名称
	 * 
	 * @return
	 */
	String getColName();
	
	/**
	 * 返回该属性的名字
	 * 
	 * @return
	 */
	String getFieldName();
	
	/**
	 * 返回原始的field对象
	 * 
	 * @return
	 */
	Field getField();
	
	ColumnType getColumnType();
	
}
