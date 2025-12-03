package cc.jfire.jsql.model.param;

import cc.jfire.jsql.metadata.TableEntityInfo;
import cc.jfire.jsql.model.InternalParam;
import cc.jfire.jsql.model.Model;
import cc.jfire.jsql.model.Param;

import java.util.List;

public class SpecialPkEqParam implements InternalParam
{
    private final Object                     entity;
    private final TableEntityInfo.ColumnInfo pkInfo;

    public SpecialPkEqParam(Object entity, TableEntityInfo.ColumnInfo pkInfo)
    {
        this.entity = entity;
        this.pkInfo = pkInfo;
    }

    @Override
    public void renderSql(Model model, StringBuilder builder, List<Object> paramValues)
    {
        builder.append(pkInfo.columnName() + "=?");
        paramValues.add(pkInfo.accessor().get(entity));
    }

    @Override
    public Param and(Param param)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Param or(Param param)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Param union()
    {
        throw new UnsupportedOperationException();
    }
}
