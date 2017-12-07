package com.jfireframework.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.jfireframework.sql.transfer.column.ColumnTransfer;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserDefinedColumnTransfer
{
    Class<? extends ColumnTransfer> value();
}
