package com.jfireframework.sql.dbstructure.column.impl;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.IdentityHashMap;
import java.util.Map;
import com.jfireframework.baseutil.collection.buffer.HeapByteBuf;
import com.jfireframework.sql.dbstructure.column.ColumnType;
import com.jfireframework.sql.dbstructure.column.ColumnTypeDictionary;

public class H2ColumnTypeDictionary implements ColumnTypeDictionary
{
    private Map<Class<?>, H2Type> types = new IdentityHashMap<Class<?>, H2Type>();
    
    public H2ColumnTypeDictionary()
    {
        types.put(String.class, H2Type.VARCHAR);
        types.put(boolean.class, H2Type.BOOLEAN);
        types.put(Boolean.class, H2Type.BOOLEAN);
        types.put(byte.class, H2Type.TINYINT);
        types.put(Byte.class, H2Type.TINYINT);
        types.put(short.class, H2Type.INTEGER);
        types.put(Short.class, H2Type.INTEGER);
        types.put(int.class, H2Type.INTEGER);
        types.put(Integer.class, H2Type.INTEGER);
        types.put(long.class, H2Type.BIGINT);
        types.put(Long.class, H2Type.BIGINT);
        types.put(float.class, H2Type.DOUBLE);
        types.put(Float.class, H2Type.DOUBLE);
        types.put(double.class, H2Type.DOUBLE);
        types.put(Double.class, H2Type.DOUBLE);
        types.put(byte[].class, H2Type.BLOB);
        types.put(Date.class, H2Type.TIMESTAMP);
        types.put(java.util.Date.class, H2Type.TIMESTAMP);
        types.put(Calendar.class, H2Type.TIMESTAMP);
        types.put(Time.class, H2Type.TIME);
        types.put(Timestamp.class, H2Type.TIMESTAMP);
        types.put(Clob.class, H2Type.CLOB);
        types.put(Blob.class, H2Type.BLOB);
        types.put(HeapByteBuf.class, H2Type.BLOB);
    }
    
    @Override
    public ColumnType map(Class<?> type)
    {
        return types.get(type);
    }
    
    enum H2Type implements ColumnType
    {
        BIT("1"), //
        BOOLEAN("1"), //
        TINYINT(""), //
        INTEGER("10"), //
        BIGINT("19"), //
        DOUBLE("17"), //
        VARCHAR("255"), //
        DATE("8"), //
        TIME("6"), //
        TIMESTAMP("23"), //
        BLOB("2147483647"), //
        CLOB("2147483647");//
        
        final String desc;
        
        private H2Type(String desc)
        {
            this.desc = desc;
        }
        
        @Override
        public String desc()
        {
            return desc;
        }
        
        @Override
        public String type()
        {
            return name();
        }
        
    }
    
}
