package com.jfireframework.sql.dbstructure.column.impl;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import com.jfireframework.baseutil.collection.buffer.HeapByteBuf;
import com.jfireframework.sql.dbstructure.column.ColumnType;
import com.jfireframework.sql.dbstructure.column.ColumnTypeDictionary;

public class OracleColumnTypeDictionary implements ColumnTypeDictionary
{
    private Map<Class<?>, ColumnType> types = new HashMap<Class<?>, ColumnType>();
    
    public OracleColumnTypeDictionary()
    {
        types.put(String.class, OracleColumnType.VARCHAR2);
        types.put(boolean.class, OracleColumnType.NUMBER);
        types.put(Boolean.class, OracleColumnType.NUMBER);
        types.put(byte.class, OracleColumnType.NUMBER);
        types.put(Byte.class, OracleColumnType.NUMBER);
        types.put(short.class, OracleColumnType.NUMBER);
        types.put(Short.class, OracleColumnType.NUMBER);
        types.put(int.class, OracleColumnType.NUMBER);
        types.put(Integer.class, OracleColumnType.NUMBER);
        types.put(long.class, OracleColumnType.NUMBER);
        types.put(Long.class, OracleColumnType.NUMBER);
        types.put(float.class, OracleColumnType.NUMBER);
        types.put(Float.class, OracleColumnType.NUMBER);
        types.put(double.class, OracleColumnType.NUMBER);
        types.put(Double.class, OracleColumnType.NUMBER);
        types.put(byte[].class, OracleColumnType.BLOB);
        types.put(Date.class, OracleColumnType.TIMESTAMP);
        types.put(java.util.Date.class, OracleColumnType.TIMESTAMP);
        types.put(Calendar.class, OracleColumnType.TIMESTAMP);
        types.put(Time.class, OracleColumnType.TIMESTAMP);
        types.put(Timestamp.class, OracleColumnType.TIMESTAMP);
        types.put(Clob.class, OracleColumnType.CLOB);
        types.put(Blob.class, OracleColumnType.BLOB);
        types.put(HeapByteBuf.class, OracleColumnType.BLOB);
    }
    
    @Override
    public ColumnType map(Class<?> type)
    {
        return types.get(type);
    }
    
    enum OracleColumnType implements ColumnType
    {
        NUMBER("22"),
        VARCHAR2("255"), //
        DATE(""), //
        TIMESTAMP("6"), //
        BLOB(""), //
        CLOB("");//
        //
        final String desc;
        
        private OracleColumnType(String desc)
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
