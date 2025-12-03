package cc.jfire.jsql.dialect;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * 数据库方言接口，用于处理不同数据库之间的差异。
 * <p>
 * 不同的数据库在SQL语法、参数绑定等方面存在差异，Dialect接口提供了统一的抽象层来处理这些差异。
 * JSQL框架会根据实际使用的数据库类型选择相应的Dialect实现。
 * </p>
 *
 * @see DialectDict
 * @see cc.jfire.jsql.dialect.impl.StandardDialect
 */
public interface Dialect
{
    /**
     * 填充参数到PreparedStatement中。
     * <p>
     * 该方法负责将参数列表中的值按顺序绑定到PreparedStatement对象上。
     * 不同的数据库可能对某些数据类型有特殊的处理方式，Dialect实现类需要处理这些差异。
     * </p>
     *
     * @param preparedStatement 要填充参数的PreparedStatement对象
     * @param params 参数值列表
     * @throws SQLException 如果填充参数时发生数据库错误
     */
    void fillStatement(PreparedStatement preparedStatement, List<Object> params) throws SQLException;

    /**
     * 获取当前方言对应的数据库产品类型。
     *
     * @return 数据库产品类型枚举值
     * @see DialectDict
     */
    DialectDict product();

    /**
     * 三元消费者函数式接口，用于处理特定类型的参数绑定。
     * <p>
     * 该接口用于扩展参数填充逻辑，允许对特定类型的值进行自定义处理。
     * 当accept方法返回true时，表示该消费者已处理了该参数；返回false表示需要使用默认处理逻辑。
     * </p>
     */
    @FunctionalInterface
    interface ThreeConsumer
    {
        /**
         * 尝试将值绑定到PreparedStatement的指定位置。
         *
         * @param preparedStatement 要填充参数的PreparedStatement对象
         * @param index 参数索引位置（从1开始）
         * @param value 要绑定的参数值
         * @return 如果成功处理该参数返回true，否则返回false
         * @throws SQLException 如果填充参数时发生数据库错误
         */
        boolean accept(PreparedStatement preparedStatement, int index, Object value) throws SQLException;

        /**
         * 默认的参数绑定处理方法，始终返回false表示未处理。
         *
         * @param preparedStatement 要填充参数的PreparedStatement对象
         * @param index 参数索引位置
         * @param value 要绑定的参数值
         * @return 始终返回false
         */
        static boolean defaultAccept(PreparedStatement preparedStatement, int index, Object value)
        {
            return false;
        }
    }
}
