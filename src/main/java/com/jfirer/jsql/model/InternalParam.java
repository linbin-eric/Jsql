package com.jfirer.jsql.model;

import com.jfirer.jsql.model.BaseModel;
import com.jfirer.jsql.model.Param;

import java.util.List;

public interface InternalParam extends Param
{
    void renderSql(BaseModel model,StringBuilder builder, List<Object> paramValues);

    void renderSql(Class ckass, StringBuilder builder, List<Object> paramValues);
}
