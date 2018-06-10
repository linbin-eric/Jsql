package com.jfireframework.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解表明该类是一个数据库表的映射类
 * 
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableEntity
{
    String name();
    
    /**
     * 表注解
     * 
     * @return
     */
    String comment() default "";
    
    /**
     * 该对象可以编辑表结构。如果为true，则意味着该对象参与表结构的调整流程
     * 
     * @return
     */
    boolean editable() default true;
}
