package com.jfirer.jsql.annotation;

import com.jfirer.jsql.metadata.ColumnNameStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface ColumnName
{
    /**
     * 代表数据库列的列名
     *
     * @return
     */
    String value() default "";

    Class<? extends ColumnNameStrategy> strategy() default ColumnNameStrategy.LowCase.class;
}
