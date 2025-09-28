package com.jfirer.jsql.model.model.query;

import lombok.Data;

@Data
public class FixedContentSelect implements Select
{
    final String content;
    Class<?> implClass;
    String   fieldName;

    public FixedContentSelect(String content)
    {
        this.content = content;
    }

    public FixedContentSelect(String content, Class<?> implClass, String fieldName)
    {
        this.content   = content;
        this.implClass = implClass;
        this.fieldName = fieldName;
    }

    @Override
    public String toSql()
    {
        return content;
    }

    @Override
    public Class<?> implClass()
    {
        return implClass;
    }

    @Override
    public String fieldName()
    {
        return fieldName;
    }
}
