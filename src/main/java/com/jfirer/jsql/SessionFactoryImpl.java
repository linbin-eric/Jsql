package com.jfirer.jsql;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.executor.SqlExecutor;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.session.impl.SqlSessionImpl;

import javax.sql.DataSource;

/**
 * SessionFactory的默认实现类。
 * 负责创建和管理SqlSession实例，是JSQL框架与数据库交互的入口点。
 * 
 * <p>该类需要以下依赖进行初始化:</p>
 * <ul>
 *   <li>{@link DataSource}: 数据库数据源</li>
 *   <li>{@link SqlExecutor}: SQL执行器链的头部</li>
 *   <li>{@link Dialect}: 数据库方言实现</li>
 * </ul>
 * 
 * @see SessionFactory
 * @see SqlSession
 */
public class SessionFactoryImpl implements SessionFactory
{
    private final SqlExecutor headSqlExecutor;
    private final DataSource  dataSource;
    private final Dialect     dialect;

    /**
     * 构造一个新的SessionFactoryImpl实例。
     *
     * @param headSqlExecutor SQL执行器链的头部，负责处理SQL执行逻辑
     * @param dataSource 数据源，用于获取数据库连接
     * @param dialect 数据库方言，用于处理特定数据库的SQL语法差异
     */
    public SessionFactoryImpl(SqlExecutor headSqlExecutor, DataSource dataSource, Dialect dialect)
    {
        this.headSqlExecutor = headSqlExecutor;
        this.dataSource = dataSource;
        this.dialect = dialect;
    }

    /**
     * 打开一个新的SqlSession实例。
     * 该方法会从数据源中获取一个新的数据库连接，并创建一个SqlSessionImpl实例。
     *
     * @return 新创建的SqlSession实例
     * @throws RuntimeException 如果无法从数据源获取连接或创建会话时
     * @see SessionFactory#openSession()
     * @see SqlSessionImpl
     */
    @Override
    public SqlSession openSession()
    {
        try
        {
            return new SqlSessionImpl(dataSource.getConnection(), headSqlExecutor, dialect);
        }
        catch (Throwable e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }
}
