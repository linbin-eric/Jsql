package cc.jfire.jsql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表定义注解，用于将Java类映射到数据库表。
 * <p>
 * 该注解表明被标注的类是一个数据库表的映射实体类。
 * JSQL框架会根据此注解以及类中的字段注解来生成相应的SQL语句。
 * </p>
 *
 * <p>使用示例:</p>
 * <pre>
 * &#64;TableDef("user")
 * public class User {
 *     &#64;Pk
 *     private Integer id;
 *     private String name;
 *     private Integer age;
 *     // getters and setters
 * }
 * </pre>
 *
 * @see Pk
 * @see ColumnName
 * @see TableName
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableDef
{
    /**
     * 指定数据库表名。
     *
     * @return 数据库表名
     */
    String value();
}
