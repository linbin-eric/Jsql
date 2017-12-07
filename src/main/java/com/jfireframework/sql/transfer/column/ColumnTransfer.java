package com.jfireframework.sql.transfer.column;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ColumnTransfer
{
	void initialize(Field field);
	
	/**
	 * 获取结果集中的值，并且设置到bean实例中。
	 * 
	 * @param entity bean实例
	 * @param dbColName 对应的数据库列名称
	 * @param resultSet
	 * @throws SQLException
	 */
	void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException;
	
}
