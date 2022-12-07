package com.jfirer.jsql.model;

import com.jfirer.jsql.annotation.TableDef;

import java.util.LinkedList;
import java.util.List;

public class DeleteModel extends Model
{
    @Override
    public String _getSql()
    {
        StringBuilder cache = new StringBuilder();
        cache.append("delete from ").append(entityClass.getAnnotation(TableDef.class).name()).append(" ");
        setWhereColumns(cache);
        return cache.toString();
    }

    @Override
    public List<Object> getParams()
    {
        List<Object> params = new LinkedList<Object>();
        if (whereEntries != null)
        {
            for (WhereEntry entry : whereEntries)
            {
                params.add(entry.value);
            }
        }
        return params;
    }
}
