package com.jfirer.jsql.model;

import java.util.List;

public interface InternalParam extends Param
{
    void renderSql(Model model, StringBuilder builder, List<Object> paramValues);
}
