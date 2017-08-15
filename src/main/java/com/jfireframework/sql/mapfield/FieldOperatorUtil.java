package com.jfireframework.sql.mapfield;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.buffer.HeapByteBuf;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.mapfield.impl.BooleanOperator;
import com.jfireframework.sql.mapfield.impl.ByteArrayOperator;
import com.jfireframework.sql.mapfield.impl.CalendarOperator;
import com.jfireframework.sql.mapfield.impl.DateOperator;
import com.jfireframework.sql.mapfield.impl.DoubleOperator;
import com.jfireframework.sql.mapfield.impl.EnumNameOperator;
import com.jfireframework.sql.mapfield.impl.FloatOperator;
import com.jfireframework.sql.mapfield.impl.HeapByteBufOperator;
import com.jfireframework.sql.mapfield.impl.IntOperator;
import com.jfireframework.sql.mapfield.impl.IntegerOperator;
import com.jfireframework.sql.mapfield.impl.LongOperator;
import com.jfireframework.sql.mapfield.impl.SqlDateOperator;
import com.jfireframework.sql.mapfield.impl.StringOperator;
import com.jfireframework.sql.mapfield.impl.TimeOperator;
import com.jfireframework.sql.mapfield.impl.TimestampOperator;
import com.jfireframework.sql.mapfield.impl.WBooleanOperator;
import com.jfireframework.sql.mapfield.impl.WDoubleOperator;
import com.jfireframework.sql.mapfield.impl.WFloatOperator;
import com.jfireframework.sql.mapfield.impl.WLongOperator;

public class FieldOperatorUtil
{
    private static final Map<Class<?>, FieldOperator> operators = new HashMap<Class<?>, FieldOperator>();
    static
    {
        operators.put(boolean.class, new BooleanOperator());
        operators.put(Calendar.class, new CalendarOperator());
        operators.put(java.util.Date.class, new DateOperator());
        operators.put(Date.class, new SqlDateOperator());
        operators.put(double.class, new DoubleOperator());
        operators.put(float.class, new FloatOperator());
        operators.put(long.class, new LongOperator());
        operators.put(int.class, new IntOperator());
        operators.put(String.class, new StringOperator());
        operators.put(Time.class, new TimeOperator());
        operators.put(Timestamp.class, new TimestampOperator());
        operators.put(Boolean.class, new WBooleanOperator());
        operators.put(Double.class, new WDoubleOperator());
        operators.put(Float.class, new WFloatOperator());
        operators.put(Integer.class, new IntegerOperator());
        operators.put(Long.class, new WLongOperator());
        operators.put(byte[].class, new ByteArrayOperator());
        operators.put(HeapByteBuf.class, new HeapByteBufOperator());
    }
    
    public static FieldOperator getFieldOperator(Field field)
    {
        FieldOperator operator = null;
        if (field.isAnnotationPresent(UserDefinedFieldOperator.class))
        {
            try
            {
                operator = field.getAnnotation(UserDefinedFieldOperator.class).value().newInstance();
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        Class<?> fieldType = field.getType();
        if (operator == null)
        {
            operator = operators.get(fieldType);
        }
        if (operator != null)
        {
            operator.initialize(field);
            return operator;
        }
        else if (fieldType.isEnum())
        {
            operator = new EnumNameOperator();
            operator.initialize(field);
            return operator;
        }
        else
        {
            throw new NullPointerException(StringUtil.format("属性{}.{}的类型尚未支持", field.getDeclaringClass(), field.getName()));
        }
    }
    
}
