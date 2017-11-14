package com.jfireframework.sql.dbstructure.column;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 字段注释
 * 
 * @author linbin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Comment
{
	String value();
}
