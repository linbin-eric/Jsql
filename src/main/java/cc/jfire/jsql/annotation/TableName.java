package cc.jfire.jsql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表名注解，用于在多表关联查询时指定字段所属的表。
 * <p>
 * 当查询涉及多个表且存在同名字段时，使用此注解可以明确指定
 * 某个字段应该从哪个表读取数据。
 * </p>
 *
 * <p>使用示例:</p>
 * <pre>
 * public class UserDTO {
 *     private Integer id;
 *
 *     &#64;TableName("user")
 *     private String name;
 *
 *     &#64;TableName("department")
 *     private String departmentName;
 *     // getters and setters
 * }
 * </pre>
 *
 * @see TableDef
 * @see ColumnName
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableName
{
    /**
     * 指定字段所属的表名。
     *
     * @return 表名
     */
    String value();
}
