package com.jfireframework.sql;

import com.jfireframework.sql.dao.Dao;
import com.jfireframework.sql.transfer.resultset.ResultsetTransferStore;

public interface SessionFactory
{
    static final ThreadLocal<SqlSession> CURRENT_SESSION = new ThreadLocal<SqlSession>();
    
    /**
     * 获取对应的类型的Dao对象
     * @param <T>
     * 
     * @param <T>
     * @param ckass
     * @return
     */
	<T> Dao<T> getDao(Class<T> ckass);
    
    /**
     * 根据给定的接口，返回符合sqlorm规范的接口实现
     * 
     * @param entityClass
     * @return
     */
    <T> T getMapper(Class<T> entityClass);
    
    /**
     * 获得当前线程内的SqlSession
     * 
     * @return
     */
    SqlSession getCurrentSession();
    
    /**
     * 调用getCurrentSession获得session，如果存在就返回。如果没有值，则使用openSession创建一个并且存储于线程内并返回
     * 
     * @return
     */
    SqlSession getOrCreateCurrentSession();
    
    /**
     * 重新打开一个SqlSession
     * 
     * @return
     */
    SqlSession openSession();
    
    ResultsetTransferStore getResultSetTransferStore();
    
    /**
     * 删除所有的数据
     */
    void cleanAllData();
    
}
