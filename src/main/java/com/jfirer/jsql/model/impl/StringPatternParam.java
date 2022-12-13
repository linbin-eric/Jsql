package com.jfirer.jsql.model.impl;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.BaseModel;
import com.jfirer.jsql.model.support.SFunction;

import java.util.List;

public class StringPatternParam extends InternalParamImpl
{
    public enum PatternMode
    {
        BEFORE,
        AFTER,
        NONE,
        BOTH
    }

    private final SFunction<?, ?> fn;
    private final PatternMode     mode;
    private final Object          value;
    private final PatternConsumer consumer = (tableName, columnName, builder, paramValues, value, mode) -> {
        switch (mode)
        {
            case BEFORE ->
            {
                builder.append(tableName).append('.').append(columnName).append(" like ?");
                paramValues.add("%" + value);
            }
            case AFTER ->
            {
                builder.append(tableName).append('.').append(columnName).append(" like ?");
                paramValues.add(value + "%");
            }
            case NONE ->
            {
                if (value instanceof BaseModel m)
                {
                    builder.append(tableName).append('.').append(columnName).append(" like (");
                    BaseModel.ModelResult result = m.getResult();
                    builder.append(result.sql()).append(')');
                    paramValues.addAll(result.paramValues());
                }
                else
                {
                    builder.append(tableName).append('.').append(columnName).append(" like ?");
                    paramValues.add(value);
                }
            }
            case BOTH ->
            {
                builder.append(tableName).append('.').append(columnName).append(" like ?");
                paramValues.add("%" + value + "%");
            }
        }
    };

    public StringPatternParam(SFunction<?, ?> fn, PatternMode mode, Object value)
    {
        this.fn = fn;
        this.mode = mode;
        this.value = value;
    }

    @Override
    public void renderSql(List<Record> fromAsList, StringBuilder builder, List<Object> paramValues)
    {
        BaseModel.findColumnNameAndConsumer(fromAsList, fn, (tableName, colunName) -> {
            consumer.accept(tableName, colunName, builder, paramValues, value, mode);
        });
    }

    @Override
    public void renderSql(Class ckass, StringBuilder builder, List<Object> paramValues)
    {
        TableEntityInfo entityInfo = TableEntityInfo.parse(ckass);
        consumer.accept(entityInfo.getTableName(), entityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName()).columnName(), builder, paramValues, value, mode);
    }

    @FunctionalInterface
    interface PatternConsumer
    {
        void accept(String tableName, String columnName, StringBuilder builder, List<Object> paramValues, Object value, PatternMode mode);
    }
}
