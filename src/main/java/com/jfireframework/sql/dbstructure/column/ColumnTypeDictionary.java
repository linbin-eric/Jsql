package com.jfireframework.sql.dbstructure.column;

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
import com.jfireframework.baseutil.collection.buffer.HeapByteBuf;

public interface ColumnTypeDictionary
{
	ColumnType map(Class<?> type);
	
	class StandandTypeDictionary implements ColumnTypeDictionary
	{
		private Map<Class<?>, StandandType> types = new IdentityHashMap<Class<?>, StandandType>();
		
		public StandandTypeDictionary()
		{
			types.put(String.class, StandandType.VARCHAR);
			types.put(BigDecimal.class, StandandType.NUMERIC);
			types.put(boolean.class, StandandType.BIT);
			types.put(Boolean.class, StandandType.BIT);
			types.put(byte.class, StandandType.TINYINT);
			types.put(Byte.class, StandandType.TINYINT);
			types.put(short.class, StandandType.SMALLINT);
			types.put(Short.class, StandandType.SMALLINT);
			types.put(int.class, StandandType.INTEGER);
			types.put(Integer.class, StandandType.INTEGER);
			types.put(long.class, StandandType.BIGINT);
			types.put(Long.class, StandandType.BIGINT);
			types.put(float.class, StandandType.FLOAT);
			types.put(Float.class, StandandType.FLOAT);
			types.put(double.class, StandandType.DOUBLE);
			types.put(Double.class, StandandType.DOUBLE);
			types.put(byte[].class, StandandType.VARBINARY);
			types.put(Date.class, StandandType.DATETIME);
			types.put(java.util.Date.class, StandandType.DATETIME);
			types.put(Calendar.class, StandandType.DATETIME);
			types.put(Time.class, StandandType.TIME);
			types.put(Timestamp.class, StandandType.TIMESTAMP);
			types.put(Clob.class, StandandType.CLOB);
			types.put(Blob.class, StandandType.BLOB);
			types.put(Array.class, StandandType.ARRAY);
			types.put(HeapByteBuf.class, StandandType.BLOB);
		}
		
		@Override
		public StandandType map(Class<?> type)
		{
			return types.get(type);
		}
		
	}
}
