package com.jfireframework.sql.transfer.column;

import java.lang.reflect.Field;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.buffer.HeapByteBuf;
import com.jfireframework.sql.transfer.column.impl.BooleanOperator;
import com.jfireframework.sql.transfer.column.impl.ByteArrayOperator;
import com.jfireframework.sql.transfer.column.impl.CalendarOperator;
import com.jfireframework.sql.transfer.column.impl.ClobFieldOperator;
import com.jfireframework.sql.transfer.column.impl.DateOperator;
import com.jfireframework.sql.transfer.column.impl.DoubleOperator;
import com.jfireframework.sql.transfer.column.impl.EnumNameOperator;
import com.jfireframework.sql.transfer.column.impl.FloatOperator;
import com.jfireframework.sql.transfer.column.impl.HeapByteBufOperator;
import com.jfireframework.sql.transfer.column.impl.IntOperator;
import com.jfireframework.sql.transfer.column.impl.IntegerOperator;
import com.jfireframework.sql.transfer.column.impl.LongOperator;
import com.jfireframework.sql.transfer.column.impl.SqlDateOperator;
import com.jfireframework.sql.transfer.column.impl.StringOperator;
import com.jfireframework.sql.transfer.column.impl.TimeOperator;
import com.jfireframework.sql.transfer.column.impl.TimestampOperator;
import com.jfireframework.sql.transfer.column.impl.WBooleanOperator;
import com.jfireframework.sql.transfer.column.impl.WDoubleOperator;
import com.jfireframework.sql.transfer.column.impl.WFloatOperator;
import com.jfireframework.sql.transfer.column.impl.WLongOperator;

public interface ColumnTransferDictionary
{
    /**
     * 通过字典匹配，查询对应的FieldOperator
     * 
     * @param field
     * @return
     */
    Class<? extends ColumnTransfer> dictionary(Field field);
    
    public class BuildInColumnTransferDictionary implements ColumnTransferDictionary
    {
        private final Map<Class<?>, Class<? extends ColumnTransfer>> operators = new HashMap<Class<?>, Class<? extends ColumnTransfer>>();
        
        public BuildInColumnTransferDictionary()
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
            operators.put(Clob.class, ClobFieldOperator.class);
            operators.put(HeapByteBuf.class, HeapByteBufOperator.class);
        }
        
        @Override
        public Class<? extends ColumnTransfer> dictionary(Field field)
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
