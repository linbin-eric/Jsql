package com.jfireframework.sql.dbstructure.column;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MysqlColumnDef
{
	String columnName() default "";
	
	/**
	 * 该数据仅在dataType为char或者varchar时存在意义。意味着该字符字段的最大长度
	 * 
	 * @return
	 */
	int maxCharacterLength() default -1;
	
	/**
	 * 不填写的情况下使用默认的映射
	 * 
	 * @return
	 */
	String dataType() default "";
	
	String comment() default "";
	
	int numeric_precision() default -1;
	
	int numeric_scale() default -1;
}
