package cc.jfire.jsql.executor;

import cc.jfire.jsql.dialect.Dialect;
import cc.jfire.jsql.metadata.TableEntityInfo;
import cc.jfire.jsql.transfer.ResultSetTransfer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * SQL执行器接口，负责实际的SQL语句执行。
 * <p>
 * SqlExecutor采用责任链模式，支持多个执行器链式处理SQL请求。
 * 不同的执行器可以实现不同的功能，如分页处理、SQL日志记录等。
 * </p>
 *
 * <p>JSQL框架内置了多种执行器实现：</p>
 * <ul>
 *   <li>{@link cc.jfire.jsql.executor.impl.FinalExecuteSqlExecutor} - 最终执行器，真正执行SQL</li>
 *   <li>{@link cc.jfire.jsql.executor.impl.StandardPageExecutor} - 标准分页执行器</li>
 *   <li>{@link cc.jfire.jsql.executor.impl.OraclePageExecutor} - Oracle分页执行器</li>
 *   <li>{@link cc.jfire.jsql.executor.impl.PostgresPageExecutor} - PostgreSQL分页执行器</li>
 *   <li>{@link cc.jfire.jsql.executor.impl.DuckdbPageExecutor} - DuckDB分页执行器</li>
 * </ul>
 *
 * @see ResultSetTransfer
 * @see Dialect
 */
public interface SqlExecutor
{
    /**
     * 执行更新操作（INSERT、UPDATE、DELETE）。
     *
     * @param sql SQL语句
     * @param params SQL参数列表
     * @param connection 数据库连接
     * @param dialect 数据库方言
     * @return 受影响的行数
     * @throws SQLException 如果执行SQL时发生错误
     */
    int update(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException;

    /**
     * 执行插入操作并返回自动生成的主键。
     *
     * @param sql SQL插入语句
     * @param params SQL参数列表
     * @param connection 数据库连接
     * @param dialect 数据库方言
     * @param pkInfo 主键列信息
     * @return 生成的主键值（字符串形式）
     * @throws SQLException 如果执行SQL时发生错误
     */
    String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect, TableEntityInfo.ColumnInfo pkInfo) throws SQLException;

    /**
     * 执行查询并返回结果列表。
     *
     * @param sql SQL查询语句
     * @param transfer 结果集转换器
     * @param params SQL参数列表
     * @param connection 数据库连接
     * @param dialect 数据库方言
     * @return 查询结果列表
     * @throws SQLException 如果执行SQL时发生错误
     */
    List<Object> queryList(String sql, ResultSetTransfer transfer, List<Object> params, Connection connection, Dialect dialect) throws SQLException;

    /**
     * 执行查询并返回单个结果。
     *
     * @param sql SQL查询语句
     * @param transfer 结果集转换器
     * @param params SQL参数列表
     * @param connection 数据库连接
     * @param dialect 数据库方言
     * @return 查询结果对象，如果无结果则返回null
     * @throws SQLException 如果执行SQL时发生错误
     */
    Object queryOne(String sql, ResultSetTransfer transfer, List<Object> params, Connection connection, Dialect dialect) throws SQLException;

    /**
     * 获取执行器的优先级顺序。
     * <p>
     * 数字越大，越后执行。责任链按照order值从小到大排序。
     * </p>
     *
     * @return 优先级顺序值
     */
    int order();

    /**
     * 设置下一个处理器，从而形成责任链。
     *
     * @param next 下一个SQL执行器
     */
    void setNext(SqlExecutor next);
}
