package com.jfireframework.sql.annotation.pkstrategy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;

@Retention(RetentionPolicy.RUNTIME)
public @interface PkGenerator
{
	Class<? extends Generator> value() default UUIDGenerator.class;
	
	interface Generator
	{
		Object next();
	}
	
	class UUIDGenerator implements Generator
	{
		@Override
		public Object next()
		{
			return UUID.randomUUID().toString().replace("-", "");
		}
		
	}
}
