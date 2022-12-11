package com.jfirer.jsql.model.impl;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.support.SFunction;
import com.jfirer.jsql.model.BaseModel;
import com.jfirer.jsql.model.Model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InParam extends InternalParamImpl
{
    private final SFunction<?, ?> fn;
    private final Object[]        values;
    private       FiveConsumer    consumer = (tableName, columnName, values, builder, paramValues) -> {
        builder.append(tableName).append('.').append(columnName).append(" in (");
        record WrapperData(String segment, Object paramValue) {}
        String segment = Arrays.stream(values)//
                               .map(value -> {
                                   if (value instanceof Model m)
                                   {
                                       BaseModel.ModelResult result = m.getResult();
                                       return new WrapperData("(" + result.sql() + ")", result.paramValues());
                                   }
                                   else
                                   {
                                       return new WrapperData("?", value);
                                   }
                               })//
                               .peek(data -> {
                                   if (data.paramValue instanceof List<?> l)
                                   {
                                       paramValues.addAll(l);
                                   }
                                   else
                                   {
                                       paramValues.add(data.paramValue);
                                   }
                               }).map(data -> data.segment).collect(Collectors.joining(","));
        builder.append(segment).append(" )");
    };

    public InParam(SFunction<?, ?> fn, Object... values)
    {
        this.fn = fn;
        this.values = values;
    }

    @Override
    public void renderSql(List<Record> fromAsList, StringBuilder builder, List<Object> paramValues)
    {
        BaseModel.findColumnNameAndConsumer(fromAsList, fn, (tableName, columnName) -> {
            consumer.accept(tableName, columnName, values, builder, paramValues);
        });
    }

    @Override
    public void renderSql(Class ckass, StringBuilder builder, List<Object> paramValues)
    {
        TableEntityInfo            entityInfo = TableEntityInfo.parse(ckass);
        TableEntityInfo.ColumnInfo columnInfo = entityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName());
        consumer.accept(entityInfo.getTableName(), columnInfo.columnName(), values, builder, paramValues);
    }

    @FunctionalInterface
    interface FiveConsumer
    {
        void accept(String tableName, String columnName, Object[] values, StringBuilder builder, List<Object> paramValues);
    }
}
