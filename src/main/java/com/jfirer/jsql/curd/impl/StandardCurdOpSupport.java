package com.jfirer.jsql.curd.impl;

import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.jsql.metadata.TableEntityInfo;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StandardCurdOpSupport<T> extends AbstractCurdOpSupport<T>
{
    public StandardCurdOpSupport(Class<T> ckass)
    {
        super(ckass);
    }

    @Override
    protected SqlAndFieldAccessors generateInsertWithPkNullEntry(TableEntityInfo tableEntityInfo)
    {
        StringBuilder cache = new StringBuilder();
        cache.append("insert into ").append(tableEntityInfo.getTableName()).append(" (");
        StringAndValueAccessors stringAndValueAccessors = concatNonPkColumnNames(tableEntityInfo);
        cache.append(stringAndValueAccessors.sqlSegment()).append(") values (");
        String collect = Stream.generate(() -> "?").limit(stringAndValueAccessors.list().size()).collect(Collectors.joining(","));
        cache.append(collect).append(")");
        return new SqlAndFieldAccessors(cache.toString(), stringAndValueAccessors.list().toArray(new ValueAccessor[0]));
    }
}
