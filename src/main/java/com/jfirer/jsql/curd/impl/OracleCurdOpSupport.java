package com.jfirer.jsql.curd.impl;

import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.jsql.annotation.pkstrategy.Sequence;
import com.jfirer.jsql.metadata.TableEntityInfo;

import java.lang.reflect.Field;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OracleCurdOpSupport<T> extends AbstractCurdOpSupport<T>
{
    public OracleCurdOpSupport(Class<T> ckass)
    {
        super(ckass);
    }

    @Override
    protected SqlAndFieldAccessors generateNative(TableEntityInfo tableEntityInfo)
    {
        Field pkField = tableEntityInfo.getPkInfo().getField();
        if (Number.class.isAssignableFrom(pkField.getType()) && pkField.isAnnotationPresent(Sequence.class))
        {
            StringBuilder cache = new StringBuilder();
            cache.append("insert into ").append(tableEntityInfo.getTableName()).append("(");
            cache.append(tableEntityInfo.getPkInfo().getColumnName()).append(',');
            StringAndValueAccessors stringAndValueAccessors = concatNonPkColumnNames(tableEntityInfo);
            cache.append(stringAndValueAccessors.sqlSegment()).append(") values (").append(pkField.getAnnotation(Sequence.class).value()).append(".NEXTVAL,");
            String collect = Stream.generate(() -> "?").limit(stringAndValueAccessors.list().size()).collect(Collectors.joining(","));
            cache.append(collect).append(")");
            return new SqlAndFieldAccessors(cache.toString(), stringAndValueAccessors.list().toArray(new ValueAccessor[0]));
        }
        else
        {
            throw new IllegalArgumentException("oracle数据库只支持序号作为主键，请检查类：" + tableEntityInfo.getEntityClass().getName() + "的主键注解");
        }
    }
}
