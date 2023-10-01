package com.jfirer.jsql.model.param;

import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.support.SFunction;

import java.util.List;

public class TwoFieldEqParam extends InternalParamImpl
{
    private SFunction<?, ?> fn1;
    private SFunction<?, ?> fn2;

    public TwoFieldEqParam(SFunction<?, ?> fn1, SFunction<?, ?> fn2)
    {
        this.fn1 = fn1;
        this.fn2 = fn2;
    }

    @Override
    public void renderSql(Model model, StringBuilder builder, List<Object> paramValues)
    {
        builder.append(model.findColumnName(fn1)).append(" = ").append(model.findColumnName(fn2));
    }
}
