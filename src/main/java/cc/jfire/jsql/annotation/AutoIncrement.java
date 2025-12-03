package cc.jfire.jsql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自增主键注解，用于标识主键字段使用数据库自增策略。
 * <p>
 * 该注解需要与{@link Pk}注解配合使用。当实体被插入数据库时，
 * 主键值会由数据库自动生成，插入完成后JSQL会自动获取生成的主键值并设置到实体对象中。
 * </p>
 *
 * <p>使用示例:</p>
 * <pre>
 * &#64;TableDef("user")
 * public class User {
 *     &#64;Pk
 *     &#64;AutoIncrement
 *     private Integer id;
 *
 *     private String name;
 *     // getters and setters
 * }
 * </pre>
 *
 * @see Pk
 * @see Sequence
 * @see PkGenerator
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoIncrement
{}
