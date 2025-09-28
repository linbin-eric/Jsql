package com.jfirer.jsql.model.model.query;

import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.support.SFunction;
import lombok.Data;

@Data
public class FunctionSelect implements Select
{
    private Class<?> implClass;
    private String   fieldName;
    String function;
    String asName;
    Model  model;

    public FunctionSelect(Class<?> implClass,String fieldName, String function, String asName, Model model)
    {
        this.implClass = implClass;
        this.fieldName = fieldName;
        this.function = function;
        this.asName   = asName;
        this.model    = model;
    }

    @Override
    public String toSql()
    {
        String result;
        if (function == null)
        {
            result = model.findColumnName(implClass, fieldName);
        }
        else
        {
            result = function + "(" + model.findColumnName(implClass, fieldName) + ")";
        }
        if (asName != null)
        {
            result += " as " + asName;
        }
        return result;
    }

    @Override
    public Class<?> implClass()
    {
        return implClass;
    }

    @Override
    public String fieldName()
    {
        return fieldName;
    }
}
