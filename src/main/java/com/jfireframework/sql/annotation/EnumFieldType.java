package com.jfireframework.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumFieldType
{
    /**
     * 指明该Enum类型的field在数据库的映射类型
     * 
     * @return
     */
    Class<?> value();
}
