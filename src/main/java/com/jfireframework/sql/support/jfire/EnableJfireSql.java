package com.jfireframework.sql.support.jfire;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.config.annotation.Import;
import com.jfireframework.sql.support.jfire.autoresource.AutoSession;

@Retention(RetentionPolicy.RUNTIME)
@Import({ ImportMapperLoadFactory.class, AutoSession.class, TxManager.class })
public @interface EnableJfireSql
{
    public String scanPackage();
    
    public String tableMode() default "none";
}
