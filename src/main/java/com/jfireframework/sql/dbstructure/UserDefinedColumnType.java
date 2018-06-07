package com.jfireframework.sql.dbstructure;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface UserDefinedColumnType
{
    String type();
    
    String desc() default "";
}
