package com.jfirer.jsql.model.param;

import com.jfirer.jsql.model.Model;

import java.util.List;

public class NoOpParam extends InternalParamImpl
{
    public static  final NoOpParam INSTANCE = new NoOpParam();
    @Override
    public void renderSql(Model model, StringBuilder builder, List<Object> paramValues)
    {
        builder.append("1=1");
    }


}
