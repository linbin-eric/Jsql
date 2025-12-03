package cc.jfire.jsql.model.param;

import cc.jfire.jsql.model.Model;
import cc.jfire.jsql.model.support.SFunction;

import java.util.List;

public class TwoFieldParam extends InternalParamImpl
{
    private SFunction<?, ?> fn1;
    private SFunction<?, ?> fn2;
    private String          operator;

    public TwoFieldParam(SFunction<?, ?> fn1, SFunction<?, ?> fn2, String operator)
    {
        this.fn1      = fn1;
        this.fn2      = fn2;
        this.operator = operator;
    }

    @Override
    public void renderSql(Model model, StringBuilder builder, List<Object> paramValues)
    {
        builder.append(model.findColumnName(fn1.getImplClass(), fn1.resolveFieldName())).append(operator).append(model.findColumnName(fn2.getImplClass(), fn2.resolveFieldName()));
    }
}
