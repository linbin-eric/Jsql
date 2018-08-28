package com.jfireframework.sql.curd.impl;

import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.pkstrategy.Sequence;
import com.jfireframework.sql.metadata.TableEntityInfo;
import com.jfireframework.sql.metadata.TableEntityInfo.ColumnInfo;

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
        if ( Number.class.isAssignableFrom(pkField.getType()) && pkField.isAnnotationPresent(Sequence.class) )
        {
            List<Field> list = new LinkedList<Field>();
            StringCache cache = new StringCache();
            cache.append("insert into ").append(tableEntityInfo.getTableName()).append("(");
            cache.append(tableEntityInfo.getPkInfo().getColumnName()).appendComma();
            for (ColumnInfo info : tableEntityInfo.getPropertyNameKeyMap().values())
            {
                Field field = info.getField();
                if ( field.equals(pkField) )
                {
                    continue;
                }
                cache.append(info.getColumnName()).appendComma();
                list.add(field);
            }
            cache.deleteLast().append(") values (").append(pkField.getAnnotation(Sequence.class).value()).append(".NEXTVAL,");
            int size = list.size();
            for (int i = 0; i < size; i++)
            {
                cache.append("?,");
            }
            cache.deleteLast().append(")");
        }

    }

}
