package cc.jfire.jsql.transfer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义转换器注解，用于指定字段使用自定义的ResultSetTransfer进行数据转换。
 * <p>
 * 当JSQL框架内置的转换器无法满足需求时，可以使用此注解指定自定义的转换器实现。
 * 该注解可以用在字段或方法上。
 * </p>
 *
 * <p>使用示例:</p>
 * <pre>
 * &#64;TableDef("user")
 * public class User {
 *     &#64;Pk
 *     private Integer id;
 *
 *     &#64;CustomTransfer(JsonTransfer.class)
 *     private Map&lt;String, Object&gt; metadata;
 *     // getters and setters
 * }
 *
 * // 自定义JSON转换器
 * public class JsonTransfer implements ResultSetTransfer {
 *     &#64;Override
 *     public Object transfer(ResultSet resultSet, int columnIndex) {
 *         try {
 *             String json = resultSet.getString(columnIndex);
 *             return new ObjectMapper().readValue(json, Map.class);
 *         } catch (Exception e) {
 *             throw new RuntimeException(e);
 *         }
 *     }
 * }
 * </pre>
 *
 * @see ResultSetTransfer
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomTransfer
{
    /**
     * 指定自定义的结果集转换器类。
     *
     * @return ResultSetTransfer的实现类
     */
    Class<? extends ResultSetTransfer> value();
}
