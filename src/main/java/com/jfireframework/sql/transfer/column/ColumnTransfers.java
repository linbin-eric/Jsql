package com.jfireframework.sql.transfer.column;

import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.transfer.column.impl.*;

import java.lang.reflect.Field;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ColumnTransfers
{
    private static final IdentityHashMap<Class<?>, Class<? extends ColumnTransfer>> buildIn = new IdentityHashMap<Class<?>, Class<? extends ColumnTransfer>>();

    static
    {
        buildIn.put(boolean.class, BooleanColumnTransfer.class);
        buildIn.put(Boolean.class, BooleanColumnTransfer.class);
        buildIn.put(byte[].class, ByteArrayColumnTransfer.class);
        buildIn.put(Calendar.class, CalendarColumnTransfer.class);
        buildIn.put(Clob.class, ClobColumnTransfer.class);
        buildIn.put(Date.class, DateColumnTransfer.class);
        buildIn.put(double.class, DoubleColumnTransfer.class);
        buildIn.put(Double.class, DoubleColumnTransfer.class);
        buildIn.put(float.class, FloatColumnTransfer.class);
        buildIn.put(Float.class, FloatColumnTransfer.class);
        buildIn.put(int.class, IntColumnTransfer.class);
        buildIn.put(Integer.class, IntColumnTransfer.class);
        buildIn.put(long.class, LongColumnTransfer.class);
        buildIn.put(Long.class, LongColumnTransfer.class);
        buildIn.put(java.sql.Date.class, SqlDateColumnTransfer.class);
        buildIn.put(String.class, StringColumnTransfer.class);
        buildIn.put(Time.class, TimeColumnTransfer.class);
        buildIn.put(Timestamp.class, TimestampColumnTransfer.class);
    }

    private static final Map<Field, ColumnTransfer> parsed = new ConcurrentHashMap<Field, ColumnTransfer>();

    public static ColumnTransfer parse(Field field, String columnName)
    {
        ColumnTransfer columnTransfer = parsed.get(field);
        if ( columnTransfer != null )
        {
            return columnTransfer;
        }
        if ( field.isAnnotationPresent(ColumnMap.class) )
        {
            try
            {
                columnTransfer = field.getAnnotation(ColumnMap.class).value().newInstance();
                columnTransfer.initialize(field, columnName);
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
        else if ( field.getType().isEnum() )
        {
            EnumNameTransfer enumNameTransfer = new EnumNameTransfer();
            enumNameTransfer.initialize(field, columnName);
            columnTransfer = enumNameTransfer;
        }
        else if ( buildIn.containsKey(field.getType()) )
        {
            try
            {
                columnTransfer = buildIn.get(field.getType()).newInstance();
                columnTransfer.initialize(field, columnName);
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
        else
        {
            throw new NullPointerException("无法为字段:" + field.getDeclaringClass().getName() + "." + field.getName() + "找到对应的转换器");
        }
        parsed.put(field, columnTransfer);
        return columnTransfer;
    }
}
