package com.jfireframework.sql.metadata;

public interface ColumnNameStrategy
{
	/**
	 * 将类的属性名称转换为对应的数据库列名称
	 * 
	 * @param name
	 * @return
	 */
	String toColumnName(String name);
}
