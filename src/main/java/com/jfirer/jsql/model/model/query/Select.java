package com.jfirer.jsql.model.model.query;

public interface Select
{
    String toSql();

    Class<?> implClass();

    String fieldName();
}
