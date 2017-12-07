package com.jfireframework.sql.annotation.pkstrategy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;

@Retention(RetentionPolicy.RUNTIME)
public @interface GenerateStringPk
{
	Class<? extends StringGenerator> value() default UUIDGenerator.class;
	
	interface StringGenerator
	{
		String next();
	}
	
	class UUIDGenerator implements StringGenerator
	{
		@Override
		public String next()
		{
			return UUID.randomUUID().toString().replace("-", "");
		}
		
	}
}
