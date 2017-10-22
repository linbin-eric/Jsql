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
        types.put(boolean.class, OracleColumnType.TINYINT);
        types.put(Boolean.class, OracleColumnType.BIT);
        types.put(byte.class, OracleColumnType.TINYINT);
        types.put(Byte.class, OracleColumnType.TINYINT);
        types.put(short.class, OracleColumnType.INTEGER);
        types.put(Short.class, OracleColumnType.INTEGER);
        types.put(int.class, OracleColumnType.INTEGER);
        types.put(Integer.class, OracleColumnType.INTEGER);
        types.put(long.class, OracleColumnType.INTEGER);
        types.put(Long.class, OracleColumnType.INTEGER);
        types.put(float.class, OracleColumnType.FLOAT);
        types.put(Float.class, OracleColumnType.FLOAT);
        types.put(double.class, OracleColumnType.DOUBLE);
        types.put(Double.class, OracleColumnType.DOUBLE);
        types.put(byte[].class, OracleColumnType.INTEGER);
        types.put(Date.class, OracleColumnType.DATETIME);
        types.put(java.util.Date.class, OracleColumnType.DATETIME);
        types.put(Calendar.class, OracleColumnType.DATETIME);
        types.put(Time.class, OracleColumnType.TIME);
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
        BIT("1"), //
        TINYINT(""), //
        INTEGER(""), //
        BIGINT("9"), //
        FLOAT(""), //
        DOUBLE(""), //
        VARCHAR2("255"), //
        DATE(""), //
        DATETIME(""), //
        TIME(""), //
        TIMESTAMP(""), //
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
