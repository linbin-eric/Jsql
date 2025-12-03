package cc.jfire.jsql.dialect;

/**
 * 数据库方言字典枚举，定义了JSQL框架支持的数据库类型。
 * <p>
 * 该枚举用于标识不同的数据库产品，JSQL框架会根据数据库类型
 * 选择相应的SQL执行器和分页策略。
 * </p>
 *
 * @see Dialect
 */
public enum DialectDict
{
    /**
     * H2内存数据库
     */
    H2,
    /**
     * DuckDB分析型数据库
     */
    DUCKDB,
    /**
     * MySQL数据库
     */
    MYSQL,
    /**
     * Oracle数据库
     */
    ORACLE,
    /**
     * SQLite嵌入式数据库
     */
    SQLITE,
    /**
     * PostgreSQL数据库
     */
    POSTGRESQL,
    /**
     * Microsoft SQL Server数据库
     */
    SQLSERVER;
}
