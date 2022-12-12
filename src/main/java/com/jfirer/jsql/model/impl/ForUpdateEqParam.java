package com.jfirer.jsql.model.impl;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.Param;

import java.util.List;

public class ForUpdateEqParam implements InternalParam
{
    private final Object entity;

    public ForUpdateEqParam(Object entity)
    {
        this.entity = entity;
    }

    @Override
    public void renderSql(Class ckass, StringBuilder builder, List<Object> paramValues)
    {
        TableEntityInfo            entityInfo = TableEntityInfo.parse(ckass);
        TableEntityInfo.ColumnInfo columnInfo = entityInfo.getPkInfo();
        builder.append(" ").append(columnInfo.columnName() + "=?");
        paramValues.add(entityInfo.getPkInfo().accessor().get(entity));
    }

    @Override
    public void renderSql(List<Record> fromAsList, StringBuilder builder, List<Object> paramValues)
    {
        throw new UnsupportedOperationException();
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
