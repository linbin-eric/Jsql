package com.jfireframework.sql.dbstructure.column;

public @interface ColumnDesc
{
	String type();
	
	String desc() default "";
}
