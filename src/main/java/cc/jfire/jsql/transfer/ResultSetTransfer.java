package cc.jfire.jsql.transfer;

import java.sql.ResultSet;

/**
 * 结果集转换器接口，用于将JDBC ResultSet转换为Java对象。
 * <p>
 * JSQL框架内置了多种常用类型的转换器实现，用于处理基本类型、日期时间、
 * 枚举以及自定义实体类的转换。
 * </p>
 *
 * <p>内置的转换器实现包括：</p>
 * <ul>
 *   <li>{@link cc.jfire.jsql.transfer.impl.StringTransfer} - 字符串转换器</li>
 *   <li>{@link cc.jfire.jsql.transfer.impl.IntegerTransfer} - Integer转换器</li>
 *   <li>{@link cc.jfire.jsql.transfer.impl.LongTransfer} - Long转换器</li>
 *   <li>{@link cc.jfire.jsql.transfer.impl.BeanTransfer} - 实体类转换器</li>
 *   <li>更多类型请参见{@link cc.jfire.jsql.transfer.impl}包</li>
 * </ul>
 *
 * <p>自定义转换器示例:</p>
 * <pre>
 * public class JsonTransfer implements ResultSetTransfer {
 *     &#64;Override
 *     public Object transfer(ResultSet resultSet, int columnIndex) {
 *         try {
 *             String json = resultSet.getString(columnIndex);
 *             return parseJson(json);
 *         } catch (SQLException e) {
 *             throw new RuntimeException(e);
 *         }
 *     }
 * }
 * </pre>
 *
 * @see CustomTransfer
 */
public interface ResultSetTransfer
{
    /**
     * 从结果集的第一列转换数据。
     * <p>
     * 这是一个便捷方法，等同于调用{@code transfer(resultSet, 1)}。
     * </p>
     *
     * @param resultSet JDBC结果集
     * @return 转换后的Java对象
     */
    default Object transfer(ResultSet resultSet)
    {
        return transfer(resultSet, 1);
    }

    /**
     * 从结果集的指定列转换数据。
     *
     * @param resultSet JDBC结果集
     * @param columnIndex 列索引（从1开始）
     * @return 转换后的Java对象
     */
    Object transfer(ResultSet resultSet, int columnIndex);

    /**
     * 设置转换的目标类型。
     * <p>
     * 某些转换器（如枚举转换器）需要知道目标类型才能正确转换。
     * 默认实现为空操作。
     * </p>
     *
     * @param type 目标类型
     */
    default void awareType(Class type)
    {
    }
}
