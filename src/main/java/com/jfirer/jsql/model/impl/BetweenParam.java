package com.jfirer.jsql.model.impl;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.support.SFunction;
import com.jfirer.jsql.model.BaseModel;
import com.jfirer.jsql.model.Model;

import java.util.List;

public class BetweenParam extends InternalParamImpl
{
    private final SFunction<?, ?> fn;
    private final Object          value1;
    private final Object          value2;
    private       SixConsumer     sixConsumer = (tableName, columnName, value1, value2, builder, paramValues) -> {
        builder.append(tableName).append('.').append(columnName).append(" between ");
        putValue(value1, builder, paramValues);
        builder.append(" and ");
        putValue(value2, builder, paramValues);
    };

    public BetweenParam(SFunction<?, ?> fn, Object value1, Object value2)
    {
        this.fn = fn;
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public void renderSql(List<Record> fromAsList, StringBuilder builder, List<Object> paramValues)
    {
        BaseModel.findColumnNameAndConsumer(fromAsList, fn, (tableName, columnName) -> {
            sixConsumer.accept(tableName, columnName, value1, value2, builder, paramValues);
        });
    }

    @Override
    public void renderSql(Class ckass, StringBuilder builder, List<Object> paramValues)
    {
        TableEntityInfo            entityInfo = TableEntityInfo.parse(ckass);
        TableEntityInfo.ColumnInfo columnInfo = entityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName());
        sixConsumer.accept(entityInfo.getTableName(), columnInfo.columnName(), value1, value2, builder, paramValues);
    }

    private void putValue(Object value, StringBuilder builder, List<Object> paramValues)
    {
        if (value instanceof Model m)
        {
            BaseModel.ModelResult result = m.getResult();
            builder.append(" ( ").append(result.sql()).append(" )");
            paramValues.addAll(result.paramValues());
        }
        else
        {
            builder.append(" ? ");
            paramValues.add(value);
        }
    }

    @FunctionalInterface
    interface SixConsumer
    {
        void accept(String tableName, String columnName, Object value1, Object value2, StringBuilder builder, List<Object> paramValues);
    }
}
