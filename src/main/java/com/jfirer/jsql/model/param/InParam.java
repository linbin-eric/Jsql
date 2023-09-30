package com.jfirer.jsql.model.param;

import com.jfirer.jsql.model.BaseModel;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.support.SFunction;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InParam extends InternalParamImpl
{
    public static final String IN     = " in (";
    public static final String NOT_IN = " not in (";

    public InParam(SFunction<?, ?> fn, String mode, Object... values)
    {
        super(fn);
        consumer = (columnName, builder, paramValues) -> {
            builder.append(columnName).append(mode);
            record WrapperData(String segment, Object paramValue) {}
            String segment = Arrays.stream(values)//
                                   .map(value -> {
                                       if (value instanceof Model m)
                                       {
                                           BaseModel.ModelResult result = m.getResult();
                                           return new WrapperData("(" + result.sql() + ")", result.paramValues());
                                       }
                                       else
                                       {
                                           return new WrapperData("?", value);
                                       }
                                   })//
                                   .peek(data -> {
                                       if (data.paramValue instanceof List<?> l)
                                       {
                                           paramValues.addAll(l);
                                       }
                                       else
                                       {
                                           paramValues.add(data.paramValue);
                                       }
                                   }).map(data -> data.segment).collect(Collectors.joining(","));
            builder.append(segment).append(" )");
        };
    }
}
