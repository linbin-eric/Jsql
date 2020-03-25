package com.jfirer.jsql.model;

import com.jfirer.jsql.annotation.TableDef;
import com.jfirer.jsql.metadata.TableEntityInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InsertModel extends Model
{
    private List<InsertEntry> insertEntries;

    class InsertEntry
    {
        final String propertyName;
        final Object value;

        InsertEntry(String propertyName, Object value)
        {
            this.propertyName = propertyName;
            this.value = value;
        }
    }

    public Model insert(String property, Object value)
    {
        if (insertEntries == null)
        {
            insertEntries = new LinkedList<InsertEntry>();
        }
        insertEntries.add(new InsertEntry(property, value));
        return this;
    }

    @Override
    public String _getSql()
    {
        StringBuilder cache = new StringBuilder();
        cache.append("insert into ").append(entityClass.getAnnotation(TableDef.class).name()).append(" (");
        if (insertEntries == null)
        {
            throw new NullPointerException("需要插入的属性为空");
        }
        Map<String, TableEntityInfo.ColumnInfo> columnInfoMap = TableEntityInfo.parse(entityClass).getPropertyNameKeyMap();
        for (InsertEntry each : insertEntries)
        {
            cache.append(columnInfoMap.get(each.propertyName).getColumnName()).append(',');
        }
        cache.setLength(cache.length() - 1);
        cache.append(") values(");
        int size = insertEntries.size();
        for (int i = 0; i < size; i++)
        {
            cache.append("?,");
        }
        cache.setLength(cache.length() - 1);
        cache.append(')');
        return cache.toString();
    }

    @Override
    public List<Object> getParams()
    {
        List<Object> params = new LinkedList<Object>();
        if (insertEntries != null)
        {
            for (InsertEntry insertEntry : insertEntries)
            {
                params.add(insertEntry.value);
            }
        }
        return params;
    }
}
