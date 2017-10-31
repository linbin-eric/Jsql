package com.jfireframework.sql.dao;

import java.sql.Connection;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.dialect.Dialect;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.page.PageParse;

public interface Dao extends StrategyOperation
{
	/**
	 * 初始化
	 */
	void initialize(TableMetaData metaData, SqlInterceptor[] sqlInterceptors, SessionfactoryConfig config, PageParse pageParse, Dialect dialect);
	
	/**
	 * 将对象信息保存到数据库中。如果对象id值为null，进行插入操作，否则进行更新操作
	 * 
	 * 
	 * 
	 * @param entity
	 * @param connection
	 * @return
	 */
	void save(Object entity, Connection connection);
	
	/**
	 * 将一个对象以插入数据的形式保存到数据库.该对象主键需要有值
	 * 
	 * @param entity
	 * @param connection
	 */
	void insert(Object entity, Connection connection);
	
	/**
	 * 根据主键更新数据库记录
	 * 
	 * @param entity
	 * @param connection
	 * @return
	 */
	int update(Object entity, Connection connection);
	
	/**
	 * 将对象entity所代表的数据库行删除. entity其他参数并不重要,只要id参数有存在即可.删除是根据id参数进行删除的
	 * 
	 * @param entity
	 * @param connection
	 * @return
	 */
	int delete(Object entity, Connection connection);
	
	/**
	 * 在数据库该表中，使用主键查询并且返回对象
	 * 
	 * @param pk
	 * @param connection
	 * @return
	 */
	<T> T getById(Object pk, Connection connection);
	
	/**
	 * 在数据表该表中，使用主键查询并且返回对象，但是使用某一个锁定模式
	 * 
	 * @param pk
	 * @param connection
	 * @param mode
	 * @return
	 */
	<T> T getById(Object pk, Connection connection, LockMode mode);
	
	int deleteAll(Connection connection);
	
}
