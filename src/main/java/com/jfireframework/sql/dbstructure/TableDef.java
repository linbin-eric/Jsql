package com.jfireframework.sql.dbstructure;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TableDef
{
	String tableName();
	
	/**
	 * 表注解
	 * 
	 * @return
	 */
	String comment() default "";
}
