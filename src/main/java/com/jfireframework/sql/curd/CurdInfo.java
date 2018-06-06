package com.jfireframework.sql.curd;

import java.util.List;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

public interface CurdInfo<T>
{
	/**
	 * 返回插入数据库的sql语句，并且将对象解析为参数填充到params中
	 * 
	 * @param entity
	 * @param params
	 * @return
	 */
	String insert(T entity, List<Object> params);
	
	/**
	 * 返回主键更新数据库的sql。并且将对象解析为参数填充到params中。
	 * 
	 * @param entity
	 * @param params
	 * @return
	 */
	String update(T entity, List<Object> params);
	
	/**
	 * 返回插入数据库并且自动生成主键的sql语句，并且将对象解析为参数填充到params中。
	 * 
	 * @param entity
	 * @param params
	 * @return
	 */
	String autoGeneratePkInsert(T entity, List<Object> params);
	
	/**
	 * 返回通过主键查询实体的sql，并且将参数填充到params中。
	 * 
	 * @param ckass
	 * @param pk
	 * @param params
	 * @return
	 */
	String find(Object pk, List<Object> params);
	
	String delete(Object pk, List<Object> params);
	
	/**
	 * 返回通过锁模式主键查询实体的sql，并且将参数填充到params中。
	 * 
	 * @param ckass
	 * @param pk
	 * @param mode
	 * @param params
	 * @return
	 */
	String find(Object pk, LockMode mode, List<Object> params);
	
	void setPkValue(T entity, String pk);
	
	ResultSetTransfer getBeanTransfer();
	
}
