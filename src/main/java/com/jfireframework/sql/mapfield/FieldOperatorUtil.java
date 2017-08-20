package com.jfireframework.sql.mapfield;

import java.lang.reflect.Field;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;

public class FieldOperatorUtil
{
    
    public static FieldOperator getFieldOperator(Field field, FieldOperatorDictionary dictionary)
    {
        Class<? extends FieldOperator> operatorType = null;
        if (field.isAnnotationPresent(UserDefinedFieldOperator.class))
        {
            try
            {
                operatorType = field.getAnnotation(UserDefinedFieldOperator.class).value();
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        if (operatorType == null)
        {
            operatorType = dictionary.dictionary(field);
        }
        if (operatorType != null)
        {
            FieldOperator operator;
            try
            {
                operator = operatorType.newInstance();
                operator.initialize(field);
                return operator;
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        else
        {
            throw new NullPointerException(StringUtil.format("属性{}.{}的类型尚未支持", field.getDeclaringClass(), field.getName()));
        }
    }
    
}
