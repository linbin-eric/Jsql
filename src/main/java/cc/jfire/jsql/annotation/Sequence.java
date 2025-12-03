package cc.jfire.jsql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 序列注解，用于标识主键字段使用数据库序列生成策略。
 * <p>
 * 该注解需要与{@link Pk}注解配合使用。适用于支持序列的数据库（如Oracle、PostgreSQL等），
 * 当实体被插入数据库时，JSQL会通过指定的序列获取主键值。
 * </p>
 *
 * <p>使用示例:</p>
 * <pre>
 * &#64;TableDef("user")
 * public class User {
 *     &#64;Pk
 *     &#64;Sequence("user_id_seq")
 *     private Long id;
 *
 *     private String name;
 *     // getters and setters
 * }
 * </pre>
 *
 * @see Pk
 * @see AutoIncrement
 * @see PkGenerator
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sequence
{
    /**
     * 指定数据库序列的名称。
     *
     * @return 数据库序列名
     */
    String value();
}
