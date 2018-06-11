package com.jfireframework.sql.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface StandardColumnDef
{
	String columnName() default "";
	
	/**
	 * 该数据仅在dataType为char或者varchar时存在意义。意味着该字符字段的最大长度
	 * 
	 * @return
	 */
	int maxCharacterLength() default 64;
	
	/**
	 * 不填写的情况下使用默认的映射
	 * 
	 * @return
	 */
	String dataType() default "";
	
	boolean isNullable() default true;
	
	int numeric_precision() default 10;
	
	int numeric_scale() default 2;
	
	int datetime_precision() default 6;
}
