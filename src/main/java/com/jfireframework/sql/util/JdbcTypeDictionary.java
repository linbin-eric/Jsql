package com.jfireframework.sql.util;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.IdentityHashMap;
import java.util.Map;

public interface JdbcTypeDictionary
{
    JdbcType map(Class<?> type);
    
    class MysqlJdbcTypes implements JdbcTypeDictionary
    {
        private Map<Class<?>, JdbcType> types = new IdentityHashMap<Class<?>, JdbcType>();
        
        public MysqlJdbcTypes()
        {
            types.put(String.class, JdbcType.VARCHAR);
            types.put(BigDecimal.class, JdbcType.NUMERIC);
            types.put(boolean.class, JdbcType.BIT);
            types.put(Boolean.class, JdbcType.BIT);
            types.put(byte.class, JdbcType.TINYINT);
            types.put(Byte.class, JdbcType.TINYINT);
            types.put(short.class, JdbcType.SMALLINT);
            types.put(Short.class, JdbcType.SMALLINT);
            types.put(int.class, JdbcType.INTEGER);
            types.put(Integer.class, JdbcType.INTEGER);
            types.put(long.class, JdbcType.BIGINT);
            types.put(Long.class, JdbcType.BIGINT);
            types.put(float.class, JdbcType.FLOAT);
            types.put(Float.class, JdbcType.FLOAT);
            types.put(double.class, JdbcType.DOUBLE);
            types.put(Double.class, JdbcType.DOUBLE);
            types.put(byte[].class, JdbcType.VARBINARY);
            types.put(Date.class, JdbcType.DATETIME);
            types.put(java.util.Date.class, JdbcType.DATETIME);
            types.put(Calendar.class, JdbcType.DATETIME);
            types.put(Time.class, JdbcType.TIME);
            types.put(Timestamp.class, JdbcType.TIMESTAMP);
            types.put(Clob.class, JdbcType.CLOB);
            types.put(Blob.class, JdbcType.BLOB);
            types.put(Array.class, JdbcType.ARRAY);
        }
        
        @Override
        public JdbcType map(Class<?> type)
        {
            return types.get(type);
        }
        
    }
}
