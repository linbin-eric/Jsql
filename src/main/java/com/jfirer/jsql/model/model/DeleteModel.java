package com.jfirer.jsql.model.model;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.InternalParam;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.Param;
import com.jfirer.jsql.model.support.SFunction;

import java.util.ArrayList;
import java.util.List;

public class DeleteModel implements Model
{
    private         TableEntityInfo tableEntityInfo;
    protected final List<Object>    paramValues = new ArrayList<>();
    protected       Param           where;
    protected       int             limit;

    public DeleteModel(Class ckass)
    {
        tableEntityInfo = TableEntityInfo.parse(ckass);
    }

    protected String getSql()
    {
        StringBuilder builder = new StringBuilder("delete from ");
        builder.append(tableEntityInfo.getTableName()).append(" ");
        if (where != null)
        {
            builder.append(" where ");
            ((InternalParam) where).renderSql(this, builder, paramValues);
        }
        else
        {
        }
        if (limit != 0)
        {
            builder.append(" limit ").append(limit);
        }
        return builder.toString();
    }

    @Override
    public String findColumnName(SFunction<?, ?> fn)
    {
        return tableEntityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName()).columnName();
    }

    public DeleteModel where(Param param)
    {
        this.where = param;
        return this;
    }

    public DeleteModel limit(int limit)
    {
        this.limit = limit;
        return this;
    }

    @Override
    public ModelResult getResult()
    {
        return new ModelResult(getSql(), paramValues);
    }
}
