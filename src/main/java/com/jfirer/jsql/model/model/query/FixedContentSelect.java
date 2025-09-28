package com.jfirer.jsql.model.model.query;

import lombok.Data;

@Data

public class FixedContentSelect implements  Select
{
    final String content;
    String className;
    String fieldName;
    /*---*/

    public FixedContentSelect(String content)
    {
        this.content = content;
    }

    public FixedContentSelect(String content, String className, String fieldName)
    {
        this.content   = content;
        this.className = className;
        this.fieldName = fieldName;
    }

    @Override
    public String toSql()
    {
        return content;
    }
}
