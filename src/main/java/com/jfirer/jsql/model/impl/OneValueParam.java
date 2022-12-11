package com.jfirer.jsql.model.impl;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.support.SFunction;
import com.jfirer.jsql.model.BaseModel;
import com.jfirer.jsql.model.Model;

import java.util.List;

public class OneValueParam extends InternalParamImpl
{
    private final SFunction<?, ?> fn;
    private final Object          value;
    private final String      operator;
    private       SixConsumer sixConsumer = (tableName, columnName, value, operator, builder, paramValues) -> {
        if (value instanceof Model m)
        {
            BaseModel.ModelResult result = m.getResult();
            builder.append(tableName).append('.').append(columnName).append(operator).append(" ( ").append(result.sql()).append(" )");
            paramValues.addAll(result.paramValues());
        }
        else
        {
            builder.append(tableName).append('.').append(columnName).append(operator).append(" ? ");
            paramValues.add(value);
        }
    };

    public OneValueParam(SFunction<?, ?> fn, Object value, String operator)
    {
        this.fn = fn;
        this.value = value;
        this.operator = operator;
    }

    @Override
    public void renderSql(List<Record> fromAsList, StringBuilder builder, List<Object> paramValues)
    {
        BaseModel.findColumnNameAndConsumer(fromAsList, fn, (tableName, columnName) -> {
            sixConsumer.accept(tableName, columnName, value, operator, builder, paramValues);
        });
    }

    @Override
    public void renderSql(Class ckass, StringBuilder builder, List<Object> paramValues)
    {
        TableEntityInfo            entityInfo = TableEntityInfo.parse(ckass);
        TableEntityInfo.ColumnInfo columnInfo = entityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName());
        sixConsumer.accept(entityInfo.getTableName(), columnInfo.columnName(), value, operator, builder, paramValues);
    }

    @FunctionalInterface
    interface SixConsumer
    {
        void accept(String tableName, String columnName, Object value, String operator, StringBuilder builder, List<Object> paramValues);
    }
}
