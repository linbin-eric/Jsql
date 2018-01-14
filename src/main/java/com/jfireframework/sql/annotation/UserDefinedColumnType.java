package com.jfireframework.sql.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface UserDefinedColumnType
{
    String type();
    
    String desc() default "";
}
