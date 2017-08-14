package com.jfireframework.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.jfireframework.sql.util.JdbcType;

/**
 * 表明该字段是一个数据库的映射列
 * 
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column
{
    /**
     * 映射的数据库的列名
     * 
     * @return
     */
    public String name() default "";
    
    JdbcType jdbcType() default JdbcType.ADAPTIVE;
    
    /**
     * 字段描述。用于表达长度信息
     * 
     * @return
     */
    String desc() default "";
}
