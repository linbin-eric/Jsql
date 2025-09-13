package com.jfirer.jsql.model;

import com.jfirer.jsql.model.model.*;
import com.jfirer.jsql.model.support.SFunction;

import java.util.Collection;
import java.util.List;

/**
 * SQL模型接口，用于构建和执行各种类型的SQL语句。
 * Model接口提供了丰富的静态方法来创建不同类型的SQL操作模型，
 * 包括查询、插入、更新和删除等。
 * 
 * <p>Model接口的核心是其内部的{@link ModelResult}记录类，
 * 该类封装了生成的SQL语句和对应的参数值列表。</p>
 * 
 * <p>使用示例:</p>
 * <pre>
 * // 创建查询模型
 * QueryModel query = Model.select(User::getName, User::getAge)
 *                         .from(User.class)
 *                         .where(User::getId, 1);
 *                         
 * // 获取SQL结果
 * ModelResult result = query.getResult();
 * String sql = result.sql();
 * List&lt;Object&gt; params = result.paramValues();
 * 
 * // 执行查询
 * SqlSession session = sessionFactory.openSession();
 * List&lt;User&gt; users = session.findList(query);
 * </pre>
 * 
 * @see QueryModel
 * @see InsertModel
 * @see UpdateModel
 * @see DeleteModel
 * @see BatchInsertModel
 * @see InsertEntityModel
 * @see UpdateEntityModel
 */
public interface Model
{
    /**
     * 创建一个更新模型，用于更新指定实体类的记录。
     *
     * @param ckass 实体类的Class对象
     * @return 更新模型实例
     * @see UpdateModel
     */
    static UpdateModel update(Class<?> ckass)
    {
        return new UpdateModel(ckass);
    }

    /**
     * 创建一个插入模型，用于向数据库插入新记录。
     *
     * @param ckass 实体类的Class对象
     * @return 插入模型实例
     * @see InsertModel
     */
    static InsertModel insert(Class<?> ckass)
    {
        return new InsertModel(ckass);
    }

    /**
     * 创建一个实体插入模型，用于插入指定的实体对象。
     *
     * @param <T> 实体类型
     * @param entity 要插入的实体对象
     * @return 实体插入模型实例
     * @see InsertEntityModel
     */
    static <T> InsertEntityModel insert(T entity)
    {
        return new InsertEntityModel(entity);
    }

    /**
     * 创建一个删除模型，用于从数据库删除指定实体类的记录。
     *
     * @param ckass 实体类的Class对象
     * @return 删除模型实例
     * @see DeleteModel
     */
    static DeleteModel deleteFrom(Class<?> ckass)
    {
        return new DeleteModel(ckass);
    }

    /**
     * 创建一个实体更新模型，用于更新指定的实体对象。
     * 该方法会根据实体对象的主键值来定位要更新的记录。
     *
     * @param <T> 实体类型
     * @param entity 要更新的实体对象
     * @return 实体更新模型实例
     * @see UpdateEntityModel
     */
    static <T> UpdateEntityModel update(T entity)
    {
        return new UpdateEntityModel(entity);
    }

    /**
     * 创建一个查询模型，用于查询指定实体类的属性。
     * 可以通过可变参数指定要查询的属性。
     *
     * @param <T> 实体类型
     * @param fns 要查询的属性函数列表
     * @return 查询模型实例
     * @see QueryModel
     * @see SFunction
     */
    static <T> QueryModel select(SFunction<T, ?>... fns)
    {
        QueryModel model = new QueryModel();
        for (SFunction<?, ?> fn : fns)
        {
            model.addSelect(fn);
        }
        return model;
    }

    /**
     * 创建一个批量插入模型，用于批量插入实体集合。
     *
     * @param <T> 实体类型
     * @param entities 要批量插入的实体集合
     * @return 批量插入模型实例
     * @see BatchInsertModel
     */
    static <T> BatchInsertModel batchInsert(Collection<T> entities)
    {
        return new BatchInsertModel((Collection<Object>) entities);
    }

    /**
     * 创建一个查询所有属性的查询模型。
     *
     * @return 查询模型实例
     * @see QueryModel
     */
    static QueryModel selectAll()
    {
        return new QueryModel();
    }

    /**
     * 创建一个查询指定实体类所有属性的查询模型。
     *
     * @param ckass 实体类的Class对象
     * @return 查询模型实例
     * @see QueryModel
     */
    static QueryModel selectAll(Class<?> ckass)
    {
        return new QueryModel().from(ckass);
    }

    /**
     * 创建一个带别名的查询模型，用于为查询的属性指定别名。
     *
     * @param <T> 实体类型
     * @param fn 要查询的属性函数
     * @param asName 属性别名
     * @return 查询模型实例
     * @see QueryModel
     */
    static <T> QueryModel selectAlias(SFunction<T, ?> fn, String asName)
    {
        return new QueryModel().selectAs(fn, asName);
    }

    /**
     * 创建一个带函数的查询模型，用于在查询中使用SQL函数。
     * 例如：COUNT, SUM, MAX, MIN等。
     *
     * @param <T> 实体类型
     * @param fn 要应用函数的属性函数
     * @param function SQL函数名
     * @param asName 函数结果的别名
     * @return 查询模型实例
     * @see QueryModel
     */
    static <T> QueryModel selectWithFunction(SFunction<T, ?> fn, String function, String asName)
    {
        return new QueryModel().addSelectWithFunction(fn, function, asName);
    }

    /**
     * 创建一个带函数的查询模型，不指定别名。
     * 该方法是{@link #selectWithFunction(SFunction, String, String)}的重载版本。
     *
     * @param <T> 实体类型
     * @param fn 要应用函数的属性函数
     * @param function SQL函数名
     * @return 查询模型实例
     * @see QueryModel
     */
    static <T> QueryModel selectWithFunction(SFunction<T, ?> fn, String function)
    {
        return selectWithFunction(fn, function, null);
    }

    /**
     * 创建一个COUNT查询模型，用于统计指定属性的记录数。
     *
     * @param <T> 实体类型
     * @param fn 要统计的属性函数
     * @return 查询模型实例
     * @see QueryModel
     */
    static <T> QueryModel selectCount(SFunction<T, ?> fn)
    {
        return new QueryModel().selectCount(fn);
    }

    /**
     * 创建一个COUNT查询模型，用于统计所有记录数。
     *
     * @return 查询模型实例
     * @see QueryModel
     */
    static QueryModel selectCount()
    {
        return new QueryModel().selectCount();
    }

    /**
     * 创建一个COUNT查询模型，用于统计指定实体类的记录数。
     *
     * @param ckass 实体类的Class对象
     * @return 查询模型实例
     * @see QueryModel
     */
    static QueryModel selectCount(Class<?> ckass)
    {
        return new QueryModel().from(ckass).selectCount();
    }

    /**
     * SQL模型结果记录类，封装了生成的SQL语句和对应的参数值列表。
     * 该记录类是不可变的，确保了线程安全性。
     * 
     * @param sql 生成的SQL语句
     * @param paramValues SQL参数值列表
     */
    record ModelResult(String sql, List<Object> paramValues)
    {
    }

    /**
     * 获取模型的执行结果，包括生成的SQL语句和参数值列表。
     * 这是Model接口的核心方法，所有具体的模型实现都需要提供此方法的实现。
     *
     * @return 模型结果，包含SQL语句和参数值
     * @see ModelResult
     */
    ModelResult getResult();

    /**
     * 根据属性函数查找对应的数据库列名。
     * 该方法在默认情况下会抛出UnsupportedOperationException异常，
     * 具体的模型实现类需要根据需要重写此方法。
     *
     * @param fn 属性函数
     * @return 数据库列名
     * @throws UnsupportedOperationException 如果具体实现不支持此操作
     */
    default String findColumnName(SFunction<?, ?> fn)
    {
        throw new UnsupportedOperationException();
    }
}
