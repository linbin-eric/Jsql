package com.jfireframework.sql.annotation;

import com.jfireframework.sql.metadata.ColumnNameStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ColumnNameStrategyDef
{
    Class<? extends ColumnNameStrategy> value();
}
