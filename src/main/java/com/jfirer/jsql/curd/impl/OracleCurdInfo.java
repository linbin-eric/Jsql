package com.jfirer.jsql.curd.impl;

import com.jfirer.jsql.annotation.pkstrategy.Sequence;
import com.jfirer.jsql.metadata.TableEntityInfo;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class OracleCurdInfo<T> extends AbstractCurdInfo<T>
{

    public OracleCurdInfo(Class<T> ckass)
    {
        super(ckass);
    }

    @Override
    protected void generateNative(TableEntityInfo tableEntityInfo)
    {
        Field pkField = tableEntityInfo.getPkInfo().getField();
        if (Number.class.isAssignableFrom(pkField.getType()) && pkField.isAnnotationPresent(Sequence.class))
        {
            List<Field>   list  = new LinkedList<Field>();
            StringBuilder cache = new StringBuilder();
            cache.append("insert into ").append(tableEntityInfo.getTableName()).append("(");
            cache.append(tableEntityInfo.getPkInfo().getColumnName()).append(',');
            concatNonPkColumnNames(tableEntityInfo, pkField, cache, list);
            cache.setLength(cache.length() - 1);
            cache.append(") values (").append(pkField.getAnnotation(Sequence.class).value()).append(".NEXTVAL,");
            int size = list.size();
            for (int i = 0; i < size; i++)
            {
                cache.append("?,");
            }
            cache.setLength(cache.length() - 1);
            cache.append(")");
            autoGeneratePkInsertEntry = new SqlAndFieldEntry();
            autoGeneratePkInsertEntry.sql = cache.toString();
            autoGeneratePkInsertEntry.valueAccessors = buildValueAccessors(list);
        }
    }
}
