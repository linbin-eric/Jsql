package com.jfireframework.sql.session;

import java.sql.Connection;
import java.util.List;
import com.jfireframework.sql.dao.LockMode;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;

interface baseOp
{
    /**
     * 关闭session，释放数据库链接
     */
    void close();
    
    /**
     * 启动事务,将该数据库链接设置为非自动提交模式.如果给定的隔离级别大于0，则设置本连接的隔离级别。否则采用默认形式
     */
    void beginTransAction(int isolate);
    
    /**
     * 依据事务传播策略进行事务提交请求操作（在单一事务传播情况下，内嵌事务的提交只会消耗提交数，不会真的执行提交操作）
     */
    void commit();
    
    /**
     * 提交事务到数据库，但不改变当前数据库链接的提交模式
     */
    void flush();
    
    /**
     * 事务回滚
     */
    void rollback();
    
    /**
     * 获取当前session使用的数据库链接
     * 
     * @return
     */
    Connection getConnection();
    
}

interface CurdOp
{
    /**
     * 将一个对象保存或者更新到数据库。如果对象的id属性有值，就是更新操作，如果没有值就是插入操作
     * 
     * @param <T>
     * 
     * @param entity
     * @return
     */
    <T> void save(T entity);
    
    /**
     * 批量保存一个list中的数据
     * 
     * @param <T>
     * @param entitys
     */
    <T> void batchInsert(List<T> entitys);
    
    /**
     * 删除对象所对应的表的一条记录
     * 
     * @param <T>
     * 
     * @param entityClass 代表数据库表的类对象
     * @param pk 主键
     * @return
     */
    <T> int delete(T entity);
    
    /**
     * 将一个对象以插入的形式保存到数据库
     * 
     * @param <T>
     * 
     * @param entity
     */
    <T> void insert(T entity);
    
    /**
     * 根据主键获取一条记录，并且使用该记录创造一个对象
     * 
     * @param entityClass 代表数据库表的类对象
     * @param pk 主键
     * @return 代表该行记录的对象实例
     */
    <T> T get(Class<T> entityClass, Object pk);
    
    /**
     * 根据主键获取一条记录，并且使用该记录创造一个对象.获取的时候使用给定的锁定模式
     * 
     * @param entityClass 代表数据库表的类对象
     * @param pk 主键
     * @return 代表该行记录的对象实例
     */
    <T> T get(Class<T> entityClass, Object pk, LockMode mode);
    
}

interface StrategyOp
{
    <T> T findOne(Class<T> entityClass, String strategy, Object... params);
    
    <T> List<T> findAll(Class<T> entityClass, String strategy, Object... params);
    
    <T> List<T> findPage(Class<T> entityClass, Page page, String strategy, Object... params);
    
    int update(Class<?> ckass, String strategy, Object... params);
    
    int delete(Class<?> ckass, String strategy, Object... params);
    
    int count(Class<?> ckass, String strategy, Object... params);
}

interface SqlOp
{
    int update(String sql, Object... params);
    
    <T> T query(ResultSetTransfer transfer, String sql, Object... params);
    
    <T> List<T> queryList(ResultSetTransfer transfer, String sql, Object... params);
    
    <T> List<T> queryList(ResultSetTransfer transfer, String sql, Page page, Object... params);
}

/**
 * 代表一个connection链接，提供各种dao操作入口
 * 
 * @author eric
 * 
 */
public interface SqlSession extends baseOp, CurdOp, StrategyOp, SqlOp
{
    
}
