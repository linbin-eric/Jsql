package com.jfireframework.sql.transfer.column;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ColumnTransfer
{
	void initialize(Field field, String columnName);
	
	/**
	 * 获取结果集中的值，并且设置到bean实例中。
	 * 
	 * @param entity bean实例
	 * @param resultSet
	 * @throws SQLException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	void setEntityValue(Object entity, ResultSet resultSet) throws SQLException, IllegalArgumentException, IllegalAccessException;
	
}
