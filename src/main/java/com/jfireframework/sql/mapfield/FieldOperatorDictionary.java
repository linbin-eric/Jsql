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

public interface FieldOperatorDictionary
{
    /**
     * 通过字典匹配，查询对应的FieldOperator
     * 
     * @param field
     * @return
     */
    Class<? extends FieldOperator> dictionary(Field field);
    
    public class BuildInFieldOperatorDictionary implements FieldOperatorDictionary
    {
        private final Map<Class<?>, Class<? extends FieldOperator>> operators = new HashMap<Class<?>, Class<? extends FieldOperator>>();
        
        public BuildInFieldOperatorDictionary()
        {
            operators.put(boolean.class, BooleanOperator.class);
            operators.put(Calendar.class, CalendarOperator.class);
            operators.put(java.util.Date.class, DateOperator.class);
            operators.put(Date.class, SqlDateOperator.class);
            operators.put(double.class, DoubleOperator.class);
            operators.put(float.class, FloatOperator.class);
            operators.put(long.class, LongOperator.class);
            operators.put(int.class, IntOperator.class);
            operators.put(String.class, StringOperator.class);
            operators.put(Time.class, TimeOperator.class);
            operators.put(Timestamp.class, TimestampOperator.class);
            operators.put(Boolean.class, WBooleanOperator.class);
            operators.put(Double.class, WDoubleOperator.class);
            operators.put(Float.class, WFloatOperator.class);
            operators.put(Integer.class, IntegerOperator.class);
            operators.put(Long.class, WLongOperator.class);
            operators.put(byte[].class, ByteArrayOperator.class);
            operators.put(HeapByteBuf.class, HeapByteBufOperator.class);
        }
        
        @Override
        public Class<? extends FieldOperator> dictionary(Field field)
        {
            Class<?> fieldType = field.getType();
            if (fieldType.isEnum())
            {
                return EnumNameOperator.class;
            }
            else if (operators.containsKey(fieldType))
            {
                return operators.get(fieldType);
            }
            else
            {
                throw new NullPointerException(StringUtil.format("属性{}.{}的类型尚未支持", field.getDeclaringClass(), field.getName()));
            }
        }
        
    }
}
