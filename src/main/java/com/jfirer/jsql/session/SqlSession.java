package com.jfirer.jsql.session;

import com.jfirer.jsql.curd.LockMode;
import com.jfirer.jsql.model.Model;

import java.sql.Connection;
import java.util.List;

interface ConnectionOp
{
    /**
     * 关闭session，释放数据库链接
     */
    void close();

    /**
     * 启动事务，设置提交模式为非自动提交
     */
    void beginTransAction();

    /**
     * 提交事务，并且设置提交模式为自动提交
     */
    void commit();

    /**
     * 提交事务到数据库，但不改变当前数据库链接的提交模式
     */
    void flush();

    /**
     * 事务回滚.并且设置当前数据库提交模式为自动提交
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
     * @param entity
     * @return
     */
    <T> void save(T entity);

    <T> void update(T entity);

    /**
     * 删除一个实体的记录
     *
     * @param ckass
     * @param pk
     * @return
     */
    <T> int delete(Class<T> ckass, Object pk);

    /**
     * 将一个对象以插入的形式保存到数据库
     *
     * @param <T>
     * @param entity
     */
    <T> void insert(T entity);

    /**
     * 根据主键获取一条记录，并且使用该记录创造一个对象
     *
     * @param entityClass 代表数据库表的类对象
     * @param pk          主键
     * @return 代表该行记录的对象实例
     */
    <T> T get(Class<T> entityClass, Object pk);

    /**
     * 根据主键获取一条记录，并且使用该记录创造一个对象.获取的时候使用给定的锁定模式
     *
     * @param entityClass 代表数据库表的类对象
     * @param pk          主键
     * @return 代表该行记录的对象实例
     */
    <T> T get(Class<T> entityClass, Object pk, LockMode mode);
}

interface ModelOp
{
    <T> T findOne(Model model);

    /**
     * 如果最后一个参数是Page，则会触发分页查询
     *
     * @return
     */
    <T> List<T> find(Model model);

    int count(Model model);

    int update(Model model);

    int delete(Model model);

    int insert(Model model);
}

/**
 * 代表一个connection链接，提供各种dao操作入口
 *
 * @author eric
 */
public interface SqlSession extends ConnectionOp, CurdOp, ModelOp, SqlOp
{
    <T> T getMapper(Class<T> mapperClass);
}
