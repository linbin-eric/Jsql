package com.jfirer.jsql.session;

import com.jfirer.jsql.model.Model;

import java.lang.reflect.AnnotatedElement;
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

/**
 * 代表一个connection链接，提供各种dao操作入口
 *
 * @author eric
 */
public interface SqlSession extends ConnectionOp
{
    <T> T getMapper(Class<T> mapperClass);

    <T> T findOne(Model model);

    /**
     * 如果最后一个参数是Page，则会触发分页查询
     *
     * @return
     */
    <T> List<T> findList(Model model);

    int count(Model model);

    int execute(Model model);

    /**
     * 保存一个对象到数据库,会根据该对象的主键属性是否为空进行不同的行为。
     * 1、不存在主键的，则按照全量插入处理。
     * 2.1、存在主键，且主键属性有值，按照全量插入处理。
     * 2.2、存在主键，主键属性为空，主键有PkGenerator注解，则使用对应的生成器生成主键属性，赋值给入参对象后，按照全量插入处理。
     * 2.3、存在主键，主键属性为空，主键上有AutoIncrement主键，则除了主键属性外，所有的属性均插入数据库，并且返回数据库自动生成的主键值。
     * 2.4、存在主键，主键属性为空，主键上有Sequence主键，则除了主键属性外，所有的属性均插入数据库，并且返回数据库自动生成的主键值。
     * 2.5、抛出异常
     *
     * @param <T>
     * @param entity
     * @return
     */
    <T> int save(T entity);

    <T> int update(T entity);

    /**
     * 将一个对象以插入的形式保存到数据库
     *
     * @param <T>
     * @param entity
     */
    <T> int insert(T entity);

    <T> void batchInsert(List<T> list);

    int execute(String sql, List<Object> params);

    /**
     * 插入一行数据，并且以String的形式返回自动生成的主键
     *
     * @param sql
     * @param params
     * @return
     */
    String insertReturnPk(String sql, List<Object> params);

    <T> T query(String sql, AnnotatedElement element, List<Object> params);

    /**
     * 如果最后一个参数是Page，则会触发page查询
     *
     * @param sql
     * @param params
     * @return
     */
    <T> List<T> queryList(String sql, AnnotatedElement element, List<Object> params);
}
