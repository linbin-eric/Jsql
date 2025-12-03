package cc.jfire.jsql.annotation;

import java.lang.annotation.*;

/**
 * SQL忽略注解，用于标识在SQL操作中应被忽略的字段。
 * <p>
 * 被该注解标注的字段在执行INSERT、UPDATE等操作时不会被包含在SQL语句中。
 * 该注解支持继承，子类会继承父类字段上的此注解。
 * </p>
 *
 * <p>适用场景:</p>
 * <ul>
 *   <li>实体类中的计算属性</li>
 *   <li>临时存储的业务数据</li>
 *   <li>仅用于程序内部使用的字段</li>
 * </ul>
 *
 * <p>使用示例:</p>
 * <pre>
 * &#64;TableDef("user")
 * public class User {
 *     &#64;Pk
 *     private Integer id;
 *
 *     private String name;
 *
 *     &#64;SqlIgnore
 *     private String fullName; // 该字段不会参与SQL操作
 *     // getters and setters
 * }
 * </pre>
 *
 * @author 林斌
 * @see TableDef
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SqlIgnore
{}
