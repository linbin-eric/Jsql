package com.jfirer.jsql.model;

import com.jfirer.jsql.annotation.TableDef;
import com.jfirer.jsql.metadata.TableEntityInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UpdateModel extends Model
{
    private List<UpdateEntry> setEntries;

    class UpdateEntry
    {
        final String propertyName;
        final Object value;

        UpdateEntry(String propertyName, Object value)
        {
            this.propertyName = propertyName;
            this.value = value;
        }
    }

    public UpdateModel set(String property, Object value)
    {
        if (setEntries == null)
        {
            setEntries = new LinkedList<UpdateModel.UpdateEntry>();
        }
        setEntries.add(new UpdateEntry(property, value));
        return this;
    }

    @Override
    public String _getSql()
    {
        StringBuilder cache = new StringBuilder();
        cache.append("update ").append(entityClass.getAnnotation(TableDef.class).value()).append(" ");
        if (setEntries == null)
        {
            throw new IllegalArgumentException("没有设置需要更新的字段");
        }
        cache.append("set ");
        Map<String, TableEntityInfo.ColumnInfo> columnInfoMap = TableEntityInfo.parse(entityClass).getPropertyNameKeyMap();
        for (UpdateEntry each : setEntries)
        {
            cache.append(columnInfoMap.get(each.propertyName).columnName()).append("=?,");
        }
        cache.setLength(cache.length() - 1);
        cache.append(' ');
        setWhereColumns(cache);
        return cache.toString();
    }

    @Override
    public List<Object> getParams()
    {
        List<Object> params = new LinkedList<Object>();
        if (setEntries != null)
        {
            for (UpdateEntry updateEntry : setEntries)
            {
                params.add(updateEntry.value);
            }
        }
        if (whereEntries != null)
        {
            for (WhereEntry whereEntry : whereEntries)
            {
                params.add(whereEntry.value);
            }
        }
        return params;
    }
}
