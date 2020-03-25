package com.jfirer.jsql.curd.impl;

import com.jfirer.jsql.annotation.pkstrategy.AutoIncrement;
import com.jfirer.jsql.metadata.TableEntityInfo;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class StandardCurdInfo<T> extends AbstractCurdInfo<T>
{

    public StandardCurdInfo(Class<T> ckass)
    {
        super(ckass);
    }

    @Override
    protected void generateNative(TableEntityInfo tableEntityInfo)
    {
        Field pkField = tableEntityInfo.getPkInfo().getField();
        if (Number.class.isAssignableFrom(pkField.getType()) || pkField.isAnnotationPresent(AutoIncrement.class))
        {
            StringBuilder cache = new StringBuilder();
            cache.append("insert into ").append(tableEntityInfo.getTableName()).append(" (");
            List<Field> list = new LinkedList<Field>();
            concatNonPkColumnNames(tableEntityInfo, pkField, cache, list);
            cache.setLength(cache.length() - 1);
            cache.append(") values (");
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
