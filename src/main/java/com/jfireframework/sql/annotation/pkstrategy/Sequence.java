package com.jfireframework.sql.annotation.pkstrategy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 序列策略
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Sequence
{
    /**
     * 序列的名称
     *
     * @return
     */
    String value();
}
