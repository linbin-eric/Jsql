package cc.jfire.jsql.annotation;

import cc.jfire.jsql.metadata.ColumnNameStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 列名注解，用于指定字段与数据库列之间的映射关系。
 * <p>
 * 该注解可以用在字段或类上：
 * </p>
 * <ul>
 *   <li>用在字段上：指定该字段对应的数据库列名</li>
 *   <li>用在类上：指定该类所有字段的默认列名映射策略</li>
 * </ul>
 *
 * <p>使用示例:</p>
 * <pre>
 * &#64;TableDef("user")
 * public class User {
 *     &#64;Pk
 *     private Integer id;
 *
 *     &#64;ColumnName("user_name")
 *     private String name;
 *
 *     &#64;ColumnName("create_time")
 *     private LocalDateTime createTime;
 *     // getters and setters
 * }
 * </pre>
 *
 * @see TableDef
 * @see ColumnNameStrategy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface ColumnName
{
    /**
     * 指定数据库列名。
     * <p>
     * 如果不指定（使用默认空字符串），则会根据{@link #strategy()}策略自动生成列名。
     * </p>
     *
     * @return 数据库列名，默认为空字符串
     */
    String value() default "";

    /**
     * 指定列名映射策略。
     * <p>
     * 当{@link #value()}为空时，会使用此策略根据Java字段名生成数据库列名。
     * </p>
     *
     * @return 列名映射策略类，默认为{@link ColumnNameStrategy.LowCase}
     * @see ColumnNameStrategy
     */
    Class<? extends ColumnNameStrategy> strategy() default ColumnNameStrategy.LowCase.class;
}
