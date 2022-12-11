package com.jfirer.jsql.model.impl;

import com.jfirer.jsql.model.Param;

import java.util.List;

public interface InternalParam extends Param
{
    void renderSql(List<Record> fromAsList, StringBuilder builder, List<Object> paramValues);

    void renderSql(Class ckass, StringBuilder builder, List<Object> paramValues);
}
