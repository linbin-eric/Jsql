package cc.jfire.jsql;

import cc.jfire.jsql.session.SqlSession;

/**
 * Session工厂接口，用于创建SqlSession实例。
 * SessionFactory是JSQL框架的核心入口点，负责管理数据库会话的生命周期。
 * 
 * <p>典型用法:</p>
 * <pre>
 * SessionFactory sessionFactory = new SessionFactoryImpl(...);
 * SqlSession session = sessionFactory.openSession();
 * // 执行数据库操作...
 * session.close();
 * </pre>
 * 
 * @see SessionFactoryImpl
 * @see SqlSession
 */
public interface SessionFactory
{
    /**
     * 打开一个新的SqlSession实例。
     * 每次调用都会创建一个新的数据库连接会话。
     * 使用完毕后，必须调用SqlSession.close()方法关闭会话以释放资源。
     *
     * @return 新创建的SqlSession实例
     * @see SqlSession
     */
    SqlSession openSession();
}
