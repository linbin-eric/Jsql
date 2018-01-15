package com.jfireframework.sql.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 代表着索引
 * 
 * @author linbin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Index
{
    /**
     * 默认情况下，由框架生成索引名称
     * 
     * @return
     */
    String indexName() default "";
    
    /**
     * 唯一索引，默认为false
     * 
     * @return
     */
    boolean unique() default false;
    
    /**
     * 索引类型，
     * 
     * @return
     */
    String indexType() default "";
}
