package com.jfirer.jsql.model.model;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.BaseModel;
import com.jfirer.jsql.model.InternalParam;
import com.jfirer.jsql.model.support.SFunction;

import java.util.LinkedList;
import java.util.List;

public class UpdateModel extends BaseModel
{
    private TableEntityInfo tableEntityInfo;
    private List<Set>       sets = new LinkedList<>();

    record Set(String columnName, Object value, boolean anotherField)
    {
    }

    public UpdateModel(Class ckass)
    {
        tableEntityInfo = TableEntityInfo.parse(ckass);
        type            = ModelType.update;
    }

    public <T> UpdateModel set(SFunction<T, ?> fn, Object value)
    {
        TableEntityInfo.ColumnInfo columnInfo = tableEntityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName());
        sets.add(new Set(tableEntityInfo.getTableName() + "." + columnInfo.columnName(), value, false));
        return this;
    }

    public <T, R> UpdateModel set(SFunction<T, ?> fn1, SFunction<R, ?> fn2)
    {
        if (fn2 == null)
        {
            //这种情况下，实际上是要给这个属性设置为空值
            TableEntityInfo.ColumnInfo columnInfo = tableEntityInfo.getPropertyNameKeyMap().get(fn1.resolveFieldName());
            sets.add(new Set(tableEntityInfo.getTableName() + "." + columnInfo.columnName(), null, false));
        }
        else
        {
            TableEntityInfo.ColumnInfo columnInfo1 = tableEntityInfo.getPropertyNameKeyMap().get(fn1.resolveFieldName());
            TableEntityInfo.ColumnInfo columnInfo2 = tableEntityInfo.getPropertyNameKeyMap().get(fn2.resolveFieldName());
            sets.add(new Set(tableEntityInfo.getTableName() + "." + columnInfo1.columnName(), tableEntityInfo.getTableName() + "." + columnInfo2.columnName(), true));
        }
        return this;
    }

    @Override
    protected String getSql()
    {
        StringBuilder builder = new StringBuilder("update ").append(tableEntityInfo.getTableName()).append(" set ");
        for (Set set : sets)
        {
            if (set.value instanceof BaseModel m)
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
        if (param != null)
        {
            builder.append(" where ");
            ((InternalParam) param).renderSql(this, builder, paramValues);
        }
        else
        {
        }
        return builder.toString();
    }

    @Override
    public String findColumnName(SFunction<?, ?> fn)
    {
        return tableEntityInfo.getTableName() + "." + tableEntityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName()).columnName();
    }
}
