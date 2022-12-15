package com.jfirer.jsql.model.impl;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.BaseModel;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.support.SFunction;

import java.util.List;

public class OneValueParam extends InternalParamImpl
{
    private final SFunction<?, ?> fn;
    private       SixConsumer     OneValueParamConsumer;

    public OneValueParam(SFunction<?, ?> fn, Object value, String operator)
    {
        this.fn = fn;
        OneValueParamConsumer = (columnName, builder, paramValues) -> {
            if (value instanceof Model m)
            {
                BaseModel.ModelResult result = m.getResult();
                builder.append(columnName).append(operator).append(" ( ").append(result.sql()).append(" )");
                paramValues.addAll(result.paramValues());
            }
            else
            {
                builder.append(columnName).append(operator).append(" ? ");
                paramValues.add(value);
            }
        };
    }

    @Override
    public void renderSql(BaseModel model, StringBuilder builder, List<Object> paramValues)
    {
        OneValueParamConsumer.accept(model.findColumnName(fn), builder, paramValues);
    }

    @Override
    public void renderSql(Class ckass, StringBuilder builder, List<Object> paramValues)
    {
        TableEntityInfo            entityInfo = TableEntityInfo.parse(ckass);
        TableEntityInfo.ColumnInfo columnInfo = entityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName());
        OneValueParamConsumer.accept(entityInfo.getTableName() + "." + columnInfo.columnName(), builder, paramValues);
    }

    @FunctionalInterface
    interface SixConsumer
    {
        void accept(String columnName, StringBuilder builder, List<Object> paramValues);
    }
}
