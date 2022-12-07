package com.jfirer.jsql.dbstructure;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Constraint
{
    String name() default "";

    Type type();

    enum Type
    {
        PRIMARY_KEY,
        UNIQUE_KEY
    }
}
