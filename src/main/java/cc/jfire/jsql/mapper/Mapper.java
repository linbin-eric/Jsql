package cc.jfire.jsql.mapper;

import cc.jfire.jsql.annotation.Sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mapper接口标记注解。
 * 用于标识一个接口是JSQL的Mapper接口，JSQL框架会根据该注解为接口提供自动实现。
 * 
 * <p>Mapper是JSQL框架中实现数据访问的主要方式之一。
 * 通过在接口上添加此注解，开发者可以定义数据访问方法，
 * JSQL会自动为这些方法生成实现代码。</p>
 * 
 * <p>Mapper接口可以有两种定义方式：</p>
 * <ol>
 *   <li>不继承任何接口，直接使用{@code @Mapper}注解标记，
 *       并通过注解的value属性指定相关的实体类。</li>
 *   <li>继承{@link Repository}接口，Repository接口提供了基本的CRUD方法，
 *       并通过泛型参数指定实体类型。</li>
 * </ol>
 * 
 * <p>使用示例:</p>
 * <pre>
 * // 方式1：直接使用Mapper注解
 * &#64;Mapper({User.class, Order.class})
 * public interface UserOrderMapper {
 *     &#64;Sql(sql = "SELECT * FROM user WHERE id = ${id}", paramNames = "id")
 *     User findUserById(int id);
 * }
 * 
 * // 方式2：继承Repository接口
 * &#64;Mapper
 * public interface UserMapper extends Repository&lt;User&gt; {
 *     // 自动获得Repository中定义的基本CRUD方法
 *     
 *     // 可以添加自定义方法
 *     List&lt;User&gt; findByName(String name);
 * }
 * </pre>
 * 
 * @see Repository
 * @see Sql
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mapper
{
    /**
     * 指定本Mapper中会使用的代表实体的类。
     * 这些类会被JSQL框架用来分析实体结构，生成相应的SQL语句。
     * 
     * <p>当Mapper接口继承{@link Repository}接口时，
     * 通常不需要在此处指定实体类，因为Repository接口的泛型参数已经指定了实体类型。</p>
     *
     * @return 实体类数组
     */
    Class<?>[] value() default {};
}
