package com.jfirer.jsql.model.model;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.BaseModel;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.support.SFunction;

import java.util.LinkedList;
import java.util.List;

public class InsertModel extends BaseModel
{
    private List<Insert>    inserts = new LinkedList<>();
    private TableEntityInfo tableEntityInfo;

    public InsertModel(Class ckass)
    {
        tableEntityInfo = TableEntityInfo.parse(ckass);
        type            = ModelType.insert;
    }

    record Insert(String name, Object value)
    {
    }

    public <T> InsertModel insert(SFunction<T, ?> fn, Object value)
    {
        TableEntityInfo.ColumnInfo columnInfo = tableEntityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName());
        inserts.add(new Insert(columnInfo.columnName(), value));
        return this;
    }

    @Override
    protected String getSql()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("insert into ").append(tableEntityInfo.getTableName()).append(" (");
        StringBuilder valueBuilder = new StringBuilder();
        for (Insert insert : inserts)
        {
            if (insert.value instanceof Model m)
            {
                ModelResult result = m.getResult();
                builder.append(insert.name).append(",");
                valueBuilder.append("(").append(result.sql()).append("),");
                paramValues.addAll(result.paramValues());
            }
            else
            {
                builder.append(insert.name).append(",");
                valueBuilder.append("?,");
                paramValues.add(insert.value);
            }
        }
        builder.setLength(builder.length() - 1);
        builder.append(") values (");
        builder.append(valueBuilder);
        builder.setLength(builder.length() - 1);
        return builder.append(")").toString();
    }
}
