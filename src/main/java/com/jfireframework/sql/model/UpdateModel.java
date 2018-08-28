package com.jfireframework.sql.model;

import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.TableDef;
import com.jfireframework.sql.metadata.TableEntityInfo;
import com.jfireframework.sql.metadata.TableEntityInfo.ColumnInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UpdateModel extends Model
{
    private List<UpdateEntry> setEntries;

    class UpdateEntry
    {
        String propertyName;
        Object value;

        public UpdateEntry(String propertyName, Object value)
        {
            this.propertyName = propertyName;
            this.value = value;
        }

    }

    public UpdateModel set(String property, Object value)
    {
        if ( setEntries == null )
        {
            setEntries = new LinkedList<UpdateModel.UpdateEntry>();
        }
        setEntries.add(new UpdateEntry(property, value));
        return this;
    }

    @Override
    public String _getSql()
    {
        StringCache cache = new StringCache();
        cache.append("update ").append(entityClass.getAnnotation(TableDef.class).name()).append(" ");
        if ( setEntries == null )
        {
            throw new IllegalArgumentException("没有设置需要更新的字段");
        }
        cache.append("set ");
        Map<String, ColumnInfo> columnInfoMap = TableEntityInfo.parse(entityClass).getPropertyNameKeyMap();
        for (UpdateEntry each : setEntries)
        {
            cache.append(columnInfoMap.get(each.propertyName).getColumnName()).append("=?,");
        }
        cache.deleteLast().append(' ');
        setWhereColumns(cache);
        return cache.toString();
    }

    @Override
    public List<Object> getParams()
    {
        List<Object> params = new LinkedList<Object>();
        if ( setEntries != null )
        {
            for (UpdateEntry updateEntry : setEntries)
            {
                params.add(updateEntry.value);
            }
        }
        if ( whereEntries != null )
        {
            for (WhereEntry whereEntry : whereEntries)
            {
                params.add(whereEntry.value);
            }
        }
        return params;
    }

}
