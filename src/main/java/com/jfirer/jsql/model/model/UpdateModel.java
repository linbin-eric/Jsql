package com.jfirer.jsql.model.model;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.InternalParam;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.Param;
import com.jfirer.jsql.model.support.SFunction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UpdateModel implements Model
{
    private         TableEntityInfo tableEntityInfo;
    private         List<Set>       sets        = new LinkedList<>();
    protected       Param           where;
    protected final List<Object>    paramValues = new ArrayList<>();

    record Set(String columnName, Object value, boolean anotherField)
    {
    }

    public UpdateModel(Class ckass)
    {
        tableEntityInfo = TableEntityInfo.parse(ckass);
    }

    public <T> UpdateModel set(SFunction<T, ?> fn, Object value)
    {
        TableEntityInfo.ColumnInfo columnInfo = tableEntityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName());
        sets.add(new Set(columnInfo.columnName(), value, false));
        return this;
    }

    public <T, R> UpdateModel set(SFunction<T, ?> fn1, SFunction<R, ?> fn2)
    {
        if (fn2 == null)
        {
            //这种情况下，实际上是要给这个属性设置为空值
            TableEntityInfo.ColumnInfo columnInfo = tableEntityInfo.getPropertyNameKeyMap().get(fn1.resolveFieldName());
            sets.add(new Set(columnInfo.columnName(), null, false));
        }
        else
        {
            TableEntityInfo.ColumnInfo columnInfo1 = tableEntityInfo.getPropertyNameKeyMap().get(fn1.resolveFieldName());
            TableEntityInfo.ColumnInfo columnInfo2 = tableEntityInfo.getPropertyNameKeyMap().get(fn2.resolveFieldName());
            sets.add(new Set(columnInfo1.columnName(), columnInfo2.columnName(), true));
        }
        return this;
    }

    protected String getSql()
    {
        StringBuilder builder = new StringBuilder("update ").append(tableEntityInfo.getTableName()).append(" set ");
        for (Set set : sets)
        {
            if (set.value instanceof Model m)
            {
                ModelResult result = m.getResult();
                builder.append(set.columnName).append("=(").append(result.sql()).append("),");
                paramValues.add(result.paramValues());
            }
            else if (set.anotherField)
            {
                builder.append(set.columnName).append("=").append(((String) set.value)).append(",");
            }
            else
            {
                builder.append(set.columnName).append("=?,");
                paramValues.add(set.value);
            }
        }
        builder.setLength(builder.length() - 1);
        if (where != null)
        {
            builder.append(" where ");
            ((InternalParam) where).renderSql(this, builder, paramValues);
        }
        else
        {
        }
        return builder.toString();
    }

    @Override
    public ModelResult getResult()
    {
        return new ModelResult(getSql(), paramValues);
    }

    @Override
    public String findColumnName(Class<?> ckass, String fieldName)
    {
        return tableEntityInfo.getTableName() + "." + tableEntityInfo.getPropertyNameKeyMap().get(fieldName).columnName();
    }

    public UpdateModel where(Param param)
    {
        this.where = param;
        return this;
    }
}
