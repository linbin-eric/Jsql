package com.jfirer.jsql.model.param;

import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.Param;

import java.util.List;

public class NoOpParam extends InternalParamImpl
{
    public static final NoOpParam INSTANCE = new NoOpParam();

    private NoOpParam()
    {
    }

    @Override
    public void renderSql(Model model, StringBuilder builder, List<Object> paramValues)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Param and(Param param)
    {
        if (param == INSTANCE)
        {
            return this;
        }
        else
        {
            return param;
        }
    }

    @Override
    public Param or(Param param)
    {
        if (param == INSTANCE)
        {
            return this;
        }
        else
        {
            return param;
        }
    }

    @Override
    public Param union()
    {
        throw new UnsupportedOperationException();
    }
}
