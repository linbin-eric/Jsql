package com.jfireframework.sql.curd.impl;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.pkstrategy.AutoIncrement;
import com.jfireframework.sql.util.TableEntityInfo;

public class StandardCurdInfo<T> extends AbstractCurdInfo<T>
{
    
    public StandardCurdInfo(Class<T> ckass)
    {
        super(ckass);
    }
    
    @Override
    protected void generateNative(Class<T> ckass, TableEntityInfo tableEntityInfo, Map<String, String> propertyNameToColumnNameMap, Field pkField)
    {
        if (Number.class.isAssignableFrom(pkField.getType()) || pkField.isAnnotationPresent(AutoIncrement.class))
        {
            StringCache cache = new StringCache();
            cache.append("insert into ").append(tableEntityInfo.getTableName()).append(" (");
            List<Field> list = new LinkedList<Field>();
            for (Entry<String, Field> entry : tableEntityInfo.getColumnNameToFieldMap().entrySet())
            {
                Field field = entry.getValue();
                if (field.equals(pkField))
                {
                    continue;
                }
                cache.append(entry.getKey()).append(",");
                list.add(field);
                field.setAccessible(true);
            }
            cache.deleteLast().append(") values (");
            int size = list.size();
            for (int i = 0; i < size; i++)
            {
                cache.append("?,");
            }
            cache.deleteLast().append(")");
            autoGeneratePkInsertEntry = new SqlAndFieldEntry();
            autoGeneratePkInsertEntry.sql = cache.toString();
            autoGeneratePkInsertEntry.fields = list.toArray(new Field[0]);
        }
    }
    
}
