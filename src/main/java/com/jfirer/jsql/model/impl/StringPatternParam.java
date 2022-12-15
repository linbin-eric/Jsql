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
    private final PatternConsumer consumer;

    public StringPatternParam(SFunction<?, ?> fn, PatternMode mode, Object value)
    {
        this.fn = fn;
        consumer = (columnName, builder, paramValues) -> {
            switch (mode)
            {
                case BEFORE ->
                {
                    builder.append(columnName).append(" like ?");
                    paramValues.add("%" + value);
                }
                case AFTER ->
                {
                    builder.append(columnName).append(" like ?");
                    paramValues.add(value + "%");
                }
                case NONE ->
                {
                    if (value instanceof BaseModel m)
                    {
                        builder.append(columnName).append(" like (");
                        BaseModel.ModelResult result = m.getResult();
                        builder.append(result.sql()).append(')');
                        paramValues.addAll(result.paramValues());
                    }
                    else
                    {
                        builder.append(columnName).append(" like ?");
                        paramValues.add(value);
                    }
                }
                case BOTH ->
                {
                    builder.append(columnName).append(" like ?");
                    paramValues.add("%" + value + "%");
                }
            }
        };
    }

    @Override
    public void renderSql(BaseModel model, StringBuilder builder, List<Object> paramValues)
    {
        String columnName = model.findColumnName(fn);
        consumer.accept(columnName, builder, paramValues);
    }

    @Override
    public void renderSql(Class ckass, StringBuilder builder, List<Object> paramValues)
    {
        TableEntityInfo entityInfo = TableEntityInfo.parse(ckass);
        consumer.accept(entityInfo.getTableName() + "." + entityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName()).columnName(), builder, paramValues);
    }

    @FunctionalInterface
    interface PatternConsumer
    {
        void accept(String columnName, StringBuilder builder, List<Object> paramValues);
    }
}
