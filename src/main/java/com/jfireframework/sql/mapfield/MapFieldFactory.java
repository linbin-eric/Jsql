package com.jfireframework.sql.mapfield;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import com.jfireframework.baseutil.collection.buffer.HeapByteBuf;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;
import com.jfireframework.sql.mapfield.FieldOperator.CustomFieldOperator;
import com.jfireframework.sql.mapfield.impl.BooleanOperator;
import com.jfireframework.sql.mapfield.impl.ByteArrayOperator;
import com.jfireframework.sql.mapfield.impl.CalendarOperator;
import com.jfireframework.sql.mapfield.impl.DateOperator;
import com.jfireframework.sql.mapfield.impl.DoubleOperator;
import com.jfireframework.sql.mapfield.impl.EnumNameFetcher;
import com.jfireframework.sql.mapfield.impl.FloatOperator;
import com.jfireframework.sql.mapfield.impl.HeapByteBufOperator;
import com.jfireframework.sql.mapfield.impl.IntegerOperator;
import com.jfireframework.sql.mapfield.impl.LongOperator;
import com.jfireframework.sql.mapfield.impl.MapFieldImpl;
import com.jfireframework.sql.mapfield.impl.SqlDateOperator;
import com.jfireframework.sql.mapfield.impl.StringOperator;
import com.jfireframework.sql.mapfield.impl.TimeOperator;
import com.jfireframework.sql.mapfield.impl.TimestampOperator;
import com.jfireframework.sql.mapfield.impl.WBooleanOperator;
import com.jfireframework.sql.mapfield.impl.WDoubleOperator;
import com.jfireframework.sql.mapfield.impl.WFloatOperator;
import com.jfireframework.sql.mapfield.impl.WLongOperator;

public class MapFieldFactory
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
        operators.put(int.class, new IntegerOperator());
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
    
    public static MapField getInstance(Field field, ColNameStrategy colNameStrategy)
    {
        Class<?> fieldType = field.getType();
        if (field.isAnnotationPresent(CustomFieldOperator.class))
        {
            try
            {
                FieldOperator operator = field.getAnnotation(CustomFieldOperator.class).value().newInstance();
                operator.initialize(field);
                return new MapFieldImpl(field, colNameStrategy, operator);
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        FieldOperator fieldOperator = operators.get(fieldType);
        if (fieldOperator != null)
        {
            return new MapFieldImpl(field, colNameStrategy, fieldOperator);
        }
        else if (fieldType.isEnum())
        {
            return new MapFieldImpl(field, colNameStrategy, new EnumNameFetcher());
        }
        else
        {
            Verify.error("属性{}.{}的类型尚未支持", field.getDeclaringClass(), field.getName());
            return null;
        }
    }
    
}
