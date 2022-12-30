package com.jfirer.jsql.model.impl;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.BaseModel;
import com.jfirer.jsql.model.InternalParam;
import com.jfirer.jsql.model.Param;

import java.util.List;

public class SpecialPkEqParam implements InternalParam
{
    private final Object                     entity;
    private final TableEntityInfo.ColumnInfo pkInfo;

    public SpecialPkEqParam(Object entity, TableEntityInfo.ColumnInfo pkInfo)
    {
        this.entity = entity;
        this.pkInfo = pkInfo;
    }

    @Override
    public void renderSql(BaseModel model, StringBuilder builder, List<Object> paramValues)
    {
        builder.append(pkInfo.columnName() + "=?");
        paramValues.add(pkInfo.accessor().get(entity));
    }

    @Override
    public Param and(Param param)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Param or(Param param)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Param union()
    {
        throw new UnsupportedOperationException();
    }
}
