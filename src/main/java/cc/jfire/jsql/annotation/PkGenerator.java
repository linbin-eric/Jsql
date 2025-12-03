package cc.jfire.jsql.annotation;

import cc.jfire.jsql.SessionFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

/**
 * 主键生成器注解，用于指定自定义的主键生成策略。
 * <p>
 * 该注解需要与{@link Pk}注解配合使用。允许开发者自定义主键生成逻辑，
 * 框架默认提供了{@link UUIDGenerator}作为UUID主键生成器。
 * </p>
 *
 * <p>使用示例:</p>
 * <pre>
 * // 使用默认的UUID生成器
 * &#64;TableDef("user")
 * public class User {
 *     &#64;Pk
 *     &#64;PkGenerator
 *     private String id;
 *
 *     private String name;
 *     // getters and setters
 * }
 *
 * // 使用自定义生成器
 * &#64;TableDef("order")
 * public class Order {
 *     &#64;Pk
 *     &#64;PkGenerator(CustomIdGenerator.class)
 *     private String orderId;
 *     // ...
 * }
 * </pre>
 *
 * @see Pk
 * @see AutoIncrement
 * @see Sequence
 * @see Generator
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PkGenerator
{
    /**
     * 指定主键生成器类。
     *
     * @return 主键生成器类，默认为{@link UUIDGenerator}
     */
    Class<? extends Generator> value() default UUIDGenerator.class;

    /**
     * 主键生成器接口，定义了主键生成的行为。
     * <p>
     * 实现此接口可以自定义主键生成逻辑，如雪花算法、分布式ID等。
     * </p>
     */
    interface Generator
    {
        /**
         * 生成下一个主键值。
         *
         * @return 生成的主键值
         */
        Object next();

        /**
         * 设置SessionFactory引用。
         * <p>
         * 某些生成器可能需要访问数据库来生成主键，此方法提供了SessionFactory的注入。
         * </p>
         *
         * @param sessionFactory SessionFactory实例
         */
        void setSessionFactory(SessionFactory sessionFactory);
    }

    /**
     * UUID主键生成器实现，生成32位不带连字符的UUID字符串。
     */
    class UUIDGenerator implements Generator
    {
        /**
         * 生成一个32位的UUID字符串（不含连字符）。
         *
         * @return 32位UUID字符串
         */
        @Override
        public Object next()
        {
            return UUID.randomUUID().toString().replace("-", "");
        }

        @Override
        public void setSessionFactory(SessionFactory sessionFactory)
        {
            // UUID生成不需要SessionFactory
        }
    }
}
