package com.jfirer.jsql.mapper;

import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.model.Param;

import java.util.List;

/**
 * 通用数据访问仓库接口，定义了基本的CRUD操作。
 * 该接口使用泛型来指定操作的实体类型，为具体的Mapper接口提供基础方法。
 * 
 * <p>Repository接口是JSQL框架中Mapper模式的基础接口，
 * 所有继承此接口的Mapper都会自动获得这些基本的数据访问方法。</p>
 * 
 * <p>使用示例:</p>
 * <pre>
 * &#64;Mapper(User.class)
 * public interface UserMapper extends Repository&lt;User&gt; {
 *     // 可以添加自定义的查询方法
 *     List&lt;User&gt; findByName(String name);
 * }
 * 
 * // 使用Mapper
 * UserMapper userMapper = session.getMapper(UserMapper.class);
 * User user = userMapper.findOne(User::getId, 1);
 * List&lt;User&gt; allUsers = userMapper.findList(null); // 查询所有用户
 * List&lt;User&gt; pageUsers = userMapper.findList(null, new Page().setOffset(0).setSize(10)); // 分页查询
 * </pre>
 *
 * @param <T> 实体类型
 * @see com.jfirer.jsql.mapper.Mapper
 * @see Param
 * @see Page
 */
public interface Repository<T>
{
    /**
     * 根据参数查找单个实体。
     * 如果查询结果有多条记录，只会返回第一条。
     *
     * @param param 查询参数，可以是null表示无条件查询
     * @return 查询到的实体，如果未找到则返回null
     * @see Param
     */
    T findOne(Param param);

    /**
     * 根据参数查找实体列表。
     *
     * @param param 查询参数，可以是null表示无条件查询
     * @return 查询到的实体列表
     * @see Param
     */
    List<T> findList(Param param);

    /**
     * 根据参数和分页信息查找实体列表。
     *
     * @param param 查询参数，可以是null表示无条件查询
     * @param page  分页参数
     * @return 查询到的实体列表
     * @see Param
     * @see com.jfirer.jsql.metadata.Page
     */
    List<T> findList(Param param, Page page);

    /**
     * 根据参数统计记录数量。
     *
     * @param param 查询参数，可以是null表示无条件统计
     * @return 记录数量
     * @see Param
     */
    int count(Param param);

    /**
     * 根据参数删除记录。
     *
     * @param param 删除条件参数
     * @return 受影响的记录数
     * @see Param
     */
    int delete(Param param);

    /**
     * 插入一个实体到数据库。
     *
     * @param entity 要插入的实体对象
     * @return 受影响的记录数
     */
    int insert(T entity);

    /**
     * 保存一个实体到数据库。
     * 会根据实体的主键属性是否为空来决定是执行插入还是更新操作。
     *
     * @param entity 要保存的实体对象
     * @return 受影响的记录数
     * @see com.jfirer.jsql.session.SqlSession#save(Object)
     */
    int save(T entity);

    /**
     * 更新一个实体到数据库。
     *
     * @param entity 要更新的实体对象
     * @return 受影响的记录数
     */
    int update(T entity);
}
