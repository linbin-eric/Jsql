package com.jfirer.jsql.model.model;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.BaseModel;
import com.jfirer.jsql.model.InternalParam;
import com.jfirer.jsql.model.support.SFunction;

public class DeleteModel extends BaseModel
{
    private TableEntityInfo tableEntityInfo;

    public DeleteModel(Class ckass)
    {
        tableEntityInfo = TableEntityInfo.parse(ckass);
        type            = ModelType.delete;
    }

    @Override
    protected String getSql()
    {
        StringBuilder builder = new StringBuilder("delete from ");
        builder.append(tableEntityInfo.getTableName()).append(" ");
        if (param != null)
        {
            builder.append(" where ");
            ((InternalParam) param).renderSql(this, builder, paramValues);
        }
        else
        {
        }
        return builder.toString();
    }

    @Override
    public String findColumnName(SFunction<?, ?> fn)
    {
        return tableEntityInfo.getTableName() + "." + tableEntityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName()).columnName();
    }
}
