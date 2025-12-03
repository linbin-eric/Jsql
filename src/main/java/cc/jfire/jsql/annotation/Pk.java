package cc.jfire.jsql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 主键注解，用于标识实体类中的主键字段。
 * <p>
 * 该注解表明被标注的字段是数据库表的主键。
 * 主键字段在执行更新、删除等操作时会被用作定位记录的条件。
 * </p>
 *
 * <p>对于主键的生成策略，可以配合以下注解使用：</p>
 * <ul>
 *   <li>{@link AutoIncrement} - 数据库自增主键</li>
 *   <li>{@link Sequence} - 使用数据库序列生成主键</li>
 *   <li>{@link PkGenerator} - 使用自定义生成器生成主键</li>
 * </ul>
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
 * @see TableDef
 * @see AutoIncrement
 * @see Sequence
 * @see PkGenerator
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Pk
{}
