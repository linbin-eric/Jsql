package com.jfireframework.sql.dbstructure.column;

public interface ColumnType
{
	/**
	 * 数据库字段的类型
	 * 
	 * @return
	 */
	String type();
	
	/**
	 * 该字段的描述。当存在描述的时候，最终建库的语句应该是${name}(${})
	 * 
	 * @return
	 */
	String desc();
}
