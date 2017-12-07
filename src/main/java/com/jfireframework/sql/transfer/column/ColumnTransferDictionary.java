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
import com.jfireframework.sql.transfer.column.impl.BooleanColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.ByteArrayColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.CalendarColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.ClobColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.DateColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.DoubleColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.EnumNameOperator;
import com.jfireframework.sql.transfer.column.impl.FloatColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.HeapByteBufColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.IntColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.IntegerColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.LongColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.SqlDateColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.StringColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.TimeColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.TimestampColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.WBooleanColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.WDoubleColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.WFloatColumnTransfer;
import com.jfireframework.sql.transfer.column.impl.WLongColumnTransfer;

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
            operators.put(boolean.class, BooleanColumnTransfer.class);
            operators.put(Calendar.class, CalendarColumnTransfer.class);
            operators.put(java.util.Date.class, DateColumnTransfer.class);
            operators.put(Date.class, SqlDateColumnTransfer.class);
            operators.put(double.class, DoubleColumnTransfer.class);
            operators.put(float.class, FloatColumnTransfer.class);
            operators.put(long.class, LongColumnTransfer.class);
            operators.put(int.class, IntColumnTransfer.class);
            operators.put(String.class, StringColumnTransfer.class);
            operators.put(Time.class, TimeColumnTransfer.class);
            operators.put(Timestamp.class, TimestampColumnTransfer.class);
            operators.put(Boolean.class, WBooleanColumnTransfer.class);
            operators.put(Double.class, WDoubleColumnTransfer.class);
            operators.put(Float.class, WFloatColumnTransfer.class);
            operators.put(Integer.class, IntegerColumnTransfer.class);
            operators.put(Long.class, WLongColumnTransfer.class);
            operators.put(byte[].class, ByteArrayColumnTransfer.class);
            operators.put(Clob.class, ClobColumnTransfer.class);
            operators.put(HeapByteBuf.class, HeapByteBufColumnTransfer.class);
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
