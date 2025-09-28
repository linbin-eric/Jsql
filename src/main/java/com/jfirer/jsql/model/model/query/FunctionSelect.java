package com.jfirer.jsql.model.model.query;

import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.support.SFunction;
import lombok.Data;

@Data
public class FunctionSelect implements Select
{
    SFunction<?, ?> fn;
    String          function;
    String          asName;
    Model           model;

    public FunctionSelect(SFunction<?, ?> fn, String function, String asName, Model model)
    {
        this.fn       = fn;
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
            result = model.findColumnName(fn);
        }
        else
        {
            result = function + "(" + model.findColumnName(fn) + ")";
        }
        if (asName != null)
        {
            result += " as " + asName;
        }
        return result;
    }
}
