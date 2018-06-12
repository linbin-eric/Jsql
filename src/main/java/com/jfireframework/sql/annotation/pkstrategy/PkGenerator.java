package com.jfireframework.sql.annotation.pkstrategy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;
import com.jfireframework.sql.SessionFactory;

@Retention(RetentionPolicy.RUNTIME)
public @interface PkGenerator
{
	Class<? extends Generator> value() default UUIDGenerator.class;
	
	interface Generator
	{
		Object next();
		
		void setSessionFactory(SessionFactory sessionFactory);
	}
	
	class UUIDGenerator implements Generator
	{
		@Override
		public Object next()
		{
			return UUID.randomUUID().toString().replace("-", "");
		}
		
		@Override
		public void setSessionFactory(SessionFactory sessionFactory)
		{
			// TODO Auto-generated method stub
			
		}
		
	}
}
