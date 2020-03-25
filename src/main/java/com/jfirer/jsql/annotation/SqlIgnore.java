package com.jfirer.jsql.annotation;

import java.lang.annotation.*;

/**
 * 使用该注解表明该字段会被忽略
 *
 * @author 林斌
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SqlIgnore
{

}
