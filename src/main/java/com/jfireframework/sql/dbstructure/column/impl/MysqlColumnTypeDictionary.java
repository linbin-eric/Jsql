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

public class MysqlColumnTypeDictionary implements ColumnTypeDictionary
{
    
    private Map<Class<?>, MysqlType> types = new IdentityHashMap<Class<?>, MysqlType>();
    
    public MysqlColumnTypeDictionary()
    {
        types.put(String.class, MysqlType.VARCHAR);
        types.put(boolean.class, MysqlType.BIT);
        types.put(Boolean.class, MysqlType.BIT);
        types.put(byte.class, MysqlType.TINYINT);
        types.put(Byte.class, MysqlType.TINYINT);
        types.put(short.class, MysqlType.INT);
        types.put(Short.class, MysqlType.INT);
        types.put(int.class, MysqlType.INT);
        types.put(Integer.class, MysqlType.INT);
        types.put(long.class, MysqlType.BIGINT);
        types.put(Long.class, MysqlType.BIGINT);
        types.put(float.class, MysqlType.FLOAT);
        types.put(Float.class, MysqlType.FLOAT);
        types.put(double.class, MysqlType.DOUBLE);
        types.put(Double.class, MysqlType.DOUBLE);
        types.put(byte[].class, MysqlType.BLOB);
        types.put(Date.class, MysqlType.DATETIME);
        types.put(java.util.Date.class, MysqlType.DATETIME);
        types.put(Calendar.class, MysqlType.DATETIME);
        types.put(Time.class, MysqlType.TIME);
        types.put(Timestamp.class, MysqlType.TIMESTAMP);
        types.put(Clob.class, MysqlType.CLOB);
        types.put(Blob.class, MysqlType.BLOB);
        types.put(HeapByteBuf.class, MysqlType.BLOB);
    }
    
    @Override
    public ColumnType map(Class<?> type)
    {
        return types.get(type);
    }
    
    enum MysqlType implements ColumnType
    {
        BIT("1"), //
        TINYINT(""), //
        INT("9"), //
        BIGINT("9"), //
        FLOAT(""), //
        DOUBLE(""), //
        VARCHAR("255"), //
        DATE(""), //
        DATETIME(""), //
        TIME(""), //
        TIMESTAMP(""), //
        BLOB(""), //
        CLOB("");//
        
        final String desc;
        
        private MysqlType(String desc)
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
