package com.jfirer.jsql.model.model;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.Model;

import java.util.ArrayList;
import java.util.List;

public class UpdateEntityModel implements Model
{
    private         TableEntityInfo tableEntityInfo;
    private         String          sql;
    protected final List<Object>    paramValues = new ArrayList<>();

    public UpdateEntityModel(Object entity)
    {
        tableEntityInfo = TableEntityInfo.parse(entity.getClass());
        StringBuilder builder = new StringBuilder("update ");
        builder.append(tableEntityInfo.getTableName()).append(" set ");
        for (TableEntityInfo.ColumnInfo each : tableEntityInfo.getAllColumnInfosExcludePk())
        {
            builder.append(each.columnName()).append("=?,");
            paramValues.add(each.accessor().get(entity));
        }
        builder.setLength(builder.length() - 1);
        builder.append(" where ").append(tableEntityInfo.getPkInfo().columnName()).append(" =?");
        paramValues.add(tableEntityInfo.getPkInfo().accessor().get(entity));
        sql = builder.toString();
    }

    @Override
    public ModelResult getResult()
    {
        return new ModelResult(sql, paramValues, null, null);
    }
}
