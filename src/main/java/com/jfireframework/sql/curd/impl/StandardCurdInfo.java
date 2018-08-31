package com.jfireframework.sql.curd.impl;

import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.pkstrategy.AutoIncrement;
import com.jfireframework.sql.metadata.TableEntityInfo;

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
        if ( Number.class.isAssignableFrom(pkField.getType()) || pkField.isAnnotationPresent(AutoIncrement.class) )
        {
            StringCache cache = new StringCache();
            cache.append("insert into ").append(tableEntityInfo.getTableName()).append(" (");
            List<Field> list = new LinkedList<Field>();
            concatNonPkColumnNames(tableEntityInfo, pkField, cache, list);
            cache.deleteLast().append(") values (");
            int size = list.size();
            for (int i = 0; i < size; i++)
            {
                cache.append("?,");
            }
            cache.deleteLast().append(")");
            autoGeneratePkInsertEntry = new SqlAndFieldEntry();
            autoGeneratePkInsertEntry.sql = cache.toString();
            autoGeneratePkInsertEntry.valueAccessors = buildValueAccessors(list);
        }
    }

}
