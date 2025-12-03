package cc.jfire.jsql.session;

import cc.jfire.jsql.mapper.Mapper;
import cc.jfire.jsql.metadata.Page;
import cc.jfire.jsql.metadata.TableEntityInfo;
import cc.jfire.jsql.model.Model;
import cc.jfire.jsql.model.model.QueryModel;
import cc.jfire.jsql.transfer.ResultSetTransfer;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

/**
 * 内部接口，定义了连接操作的基本方法。
 * 实现了AutoCloseable接口，确保资源可以被正确关闭。
 */
interface ConnectionOp extends AutoCloseable
{
    /**
     * 关闭关联的数据库连接。
     * 该方法会释放与当前会话关联的所有数据库资源。
     */
    void close();

    /**
     * 获取当前session使用的数据库连接。
     *
     * @return 当前数据库连接实例
     */
    Connection getConnection();
}

/**
 * 代表一个数据库连接会话，提供各种数据访问操作的入口。
 * SqlSession是JSQL框架中执行数据库操作的核心接口。
 * 
 * <p>SqlSession提供了多种数据操作方法，包括:</p>
 * <ul>
 *   <li>基本的CRUD操作</li>
 *   <li>基于Mapper接口的访问</li>
 *   <li>批量插入操作</li>
 *   <li>分页查询</li>
 *   <li>自定义SQL查询</li>
 * </ul>
 * 
 * <p>使用示例:</p>
 * <pre>
 * SessionFactory sessionFactory = ...;
 * SqlSession session = sessionFactory.openSession();
 * try {
 *     // 执行数据库操作
 *     User user = session.findOne(Model.select().from(User.class).where(...));
 * } finally {
 *     session.close(); // 确保关闭会话以释放资源
 * }
 * </pre>
 * 
 * @author eric
 * @see ConnectionOp
 */
public interface SqlSession extends ConnectionOp
{
    /**
     * 获取指定Mapper接口的实现实例。
     * Mapper是JSQL中用于定义数据访问方法的接口，通过该方法可以获取其自动生成的实现。
     *
     * @param <T> Mapper接口类型
     * @param mapperClass Mapper接口的Class对象
     * @return Mapper接口的实现实例
     * @see Mapper
     */
    <T> T getMapper(Class<T> mapperClass);

    /**
     * 批量插入实体集合到数据库。
     * 该方法会将集合中的实体分批插入到数据库中，以提高插入效率。
     *
     * @param <T> 实体类型
     * @param collection 要插入的实体集合
     * @param batchSize 每批次插入的记录数
     * @see Model#batchInsert(Collection)
     */
    <T> void batchInsert(Collection<T> collection, int batchSize);

    /**
     * 根据查询模型查找单个实体。
     * 如果查询结果有多条记录，只会返回第一条。
     *
     * @param <T> 返回实体类型
     * @param model 查询模型
     * @return 查询到的实体，如果未找到则返回null
     * @see QueryModel
     * @see Model#select(SFunction[])
     */
    <T> T findOne(QueryModel model);

    /**
     * 根据查询模型查找实体列表。
     * 如果查询模型中包含分页参数，则会执行分页查询。
     *
     * @param <T> 返回实体类型
     * @param model 查询模型
     * @return 查询到的实体列表
     * @see QueryModel
     * @see Page
     */
    <T> List<T> findList(QueryModel model);

    /**
     * 以分页的形式查询数据。
     * 会返回单次查询的数据内容和总条数。
     * 默认情况下，返回数据总数为：10，偏移量为：0.
     *
     * @param model 查询模型
     * @return 分页结果，包含数据列表和总记录数
     * @see Page
     * @see QueryModel
     */
    Page findListByPage(QueryModel model);

    /**
     * 根据模型统计记录数量。
     * 通常用于执行COUNT查询。
     *
     * @param model 查询模型
     * @return 记录数量
     * @see Model#selectCount()
     */
    int count(Model model);

    /**
     * 执行模型定义的SQL操作。
     * 可以是INSERT、UPDATE或DELETE操作。
     *
     * @param model SQL操作模型
     * @return 受影响的记录数
     * @see Model
     */
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
     * @param <T> 实体类型
     * @param entity 要保存的实体对象
     * @return 受影响的记录数
     * @see #insert(Object)
     * @see #update(Object)
     */
    <T> int save(T entity);

    /**
     * 更新一个实体对象到数据库。
     * 该方法会根据实体的主键值更新对应的记录。
     *
     * @param <T> 实体类型
     * @param entity 要更新的实体对象
     * @return 受影响的记录数
     */
    <T> int update(T entity);

    /**
     * 将一个对象以插入的形式保存到数据库。
     * 该方法会将实体的所有属性插入到数据库中。
     *
     * @param <T> 实体类型
     * @param entity 要插入的实体对象
     * @return 受影响的记录数
     */
    <T> int insert(T entity);

    /**
     * 执行指定的SQL语句。
     * 该方法允许执行任意的SQL语句，包括INSERT、UPDATE、DELETE等。
     *
     * @param sql SQL语句
     * @param params SQL参数列表
     * @return 受影响的记录数
     */
    int execute(String sql, List<Object> params);

    /**
     * 插入一行数据，并且以String的形式返回自动生成的主键。
     * 该方法用于处理数据库自动生成主键的场景。
     *
     * @param sql SQL插入语句
     * @param params SQL参数列表
     * @param pkInfo 主键信息
     * @return 生成的主键值
     */
    String insertReturnPk(String sql, List<Object> params, TableEntityInfo.ColumnInfo pkInfo);

    /**
     * 执行查询SQL并返回单个结果。
     * 该方法允许使用自定义的ResultSetTransfer来处理查询结果。
     *
     * @param <T> 返回结果类型
     * @param sql 查询SQL语句
     * @param transfer 结果集转换器
     * @param params SQL参数列表
     * @return 查询结果
     * @see ResultSetTransfer
     */
    <T> T query(String sql, ResultSetTransfer transfer, List<Object> params);

    /**
     * 执行查询SQL并返回结果列表。
     * 如果参数列表中包含Page对象，则会执行分页查询。
     * 该方法允许使用自定义的ResultSetTransfer来处理查询结果。
     *
     * @param <T> 返回结果类型
     * @param sql 查询SQL语句
     * @param transfer 结果集转换器
     * @param params SQL参数列表
     * @return 查询结果列表
     * @see ResultSetTransfer
     * @see Page
     */
    <T> List<T> queryList(String sql, ResultSetTransfer transfer, List<Object> params);
}
