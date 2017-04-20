package com.jfireframework.sql.support.jfire;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.config.annotation.BeanDefinition;

@BeanDefinition(beanName = "dataSource", //
        prototype = false, //
        className = "com.zaxxer.hikari.HikariDataSource", //
        params = { //
                "driverClassName=org.h2.Driver", //
                "jdbcUrl=jdbc:h2:mem:orderdb;DB_CLOSE_ON_EXIT=FALSE", //
                "username=sa", //
                "password=", //
                "connectionTimeout=1500", //
                "maxLifetime=1800000", //
                "maxPoolSize=2"
        })
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableH2DataSource
{
    
}
