package com.jfireframework.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表明该字段是一个数据库的主键 注意：该字段表明该字段是int型并且自增长
 * 
 * @author 林斌（windfire@zailanghua.com）
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id
{
    /**
     * 是否使用uid进行主键生成。默认为false。如果使用的情况，主键必须为Long或者String
     * 
     * @return
     */
    public boolean useUid() default false;
    
}
