package com.jfirer.jsql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sql语句的注解，注释在Mapper接口的方法上。使用该注解表明会发出对应的sql语句。
 * 如果是查询语句，根据返回结果类型有不同的含义。
 * 如果方法的返回类型是对象并且是基本类型.则返回的数据必须是单行单列.
 * 如果方法的返回类型是对象并且不是基本类型,则返回的数据是单行,并且将该行数据转换成为对象实例
 * 如果返回的类型是List<T>的形式,则根据T的类型做一进步判断.是基本类型,则数据应该是多行单列,取出即可.如果是对象类型,则按照对象实例进行转换
 *
 * @author 林斌（eric@jfire.cn）
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Sql
{
    /**
     * sql语句
     *
     * @return
     */
    String sql();

    /**
     * 方法的形参名称
     *
     * @return
     */
    String paramNames();
}
