package com.jfireframework.sql.transfer.resultset;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResultMap
{
	Class<? extends ResultSetTransfer> value();
}
