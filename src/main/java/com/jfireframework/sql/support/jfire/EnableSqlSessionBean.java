package com.jfireframework.sql.support.jfire;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.config.annotation.Import;

@Import(SqlSessionProxy.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableSqlSessionBean
{
    
}
