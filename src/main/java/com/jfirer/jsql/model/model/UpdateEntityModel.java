package com.jfirer.jsql.model.model;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.BaseModel;

public class UpdateEntityModel extends BaseModel
{
    private TableEntityInfo tableEntityInfo;
    private String          sql;

    public UpdateEntityModel(Object entity)
    {
        type            = ModelType.update;
        tableEntityInfo = TableEntityInfo.parse(entity.getClass());
        StringBuilder builder = new StringBuilder("update ");
        builder.append(tableEntityInfo.getTableName()).append(" set ");
        tableEntityInfo.getPropertyNameKeyMap().values().stream()//
                       .filter(columnInfo -> columnInfo.field() != tableEntityInfo.getPkInfo().field())//
                       .forEach(columnInfo -> {
                           builder.append(columnInfo.columnName()).append("=?,");
                           paramValues.add(columnInfo.accessor().get(entity));
                       });
        builder.setLength(builder.length() - 1);
        builder.append(" where ").append(tableEntityInfo.getPkInfo().columnName()).append(" =?");
        paramValues.add(tableEntityInfo.getPkInfo().accessor().get(entity));
        sql = builder.toString();
    }

    @Override
    protected String getSql()
    {
        return sql;
    }
}
