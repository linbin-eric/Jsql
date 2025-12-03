package cc.jfire.jsql.model.model;

import cc.jfire.jsql.metadata.TableEntityInfo;
import cc.jfire.jsql.model.InternalParam;
import cc.jfire.jsql.model.Model;
import cc.jfire.jsql.model.Param;
import cc.jfire.jsql.model.param.NoOpParam;

import java.util.ArrayList;
import java.util.List;

public class DeleteModel implements Model
{
    private         TableEntityInfo tableEntityInfo;
    protected final List<Object>    paramValues = new ArrayList<>();
    protected       Param           where;
    protected       int             limit;

    public DeleteModel(Class ckass)
    {
        tableEntityInfo = TableEntityInfo.parse(ckass);
    }

    protected String getSql()
    {
        StringBuilder builder = new StringBuilder("delete from ");
        builder.append(tableEntityInfo.getTableName()).append(" ");
        if (where != null && where != NoOpParam.INSTANCE)
        {
            builder.append(" where ");
            ((InternalParam) where).renderSql(this, builder, paramValues);
        }
        else
        {
        }
        if (limit != 0)
        {
            builder.append(" limit ").append(limit);
        }
        return builder.toString();
    }

    @Override
    public String findColumnName(Class<?> ckass, String fieldName)
    {
        return tableEntityInfo.getPropertyNameKeyMap().get(fieldName).columnName();
    }

    public DeleteModel where(Param param)
    {
        this.where = param;
        return this;
    }

    public DeleteModel limit(int limit)
    {
        this.limit = limit;
        return this;
    }

    @Override
    public ModelResult getResult()
    {
        return new ModelResult(getSql(), paramValues);
    }
}
