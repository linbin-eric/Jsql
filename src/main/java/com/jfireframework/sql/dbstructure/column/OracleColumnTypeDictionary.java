package com.jfireframework.sql.dbstructure.column;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import com.jfireframework.baseutil.collection.buffer.HeapByteBuf;

public class OracleColumnTypeDictionary implements ColumnTypeDictionary
{
	private Map<Class<?>, ColumnType> types = new HashMap<Class<?>, ColumnType>();
	
	public OracleColumnTypeDictionary()
	{
		types.put(String.class, OracleColumnType.VARCHAR);
		types.put(BigDecimal.class, OracleColumnType.NUMBER);
		types.put(boolean.class, OracleColumnType.TINYINT);
		types.put(Boolean.class, OracleColumnType.BIT);
		types.put(byte.class, OracleColumnType.TINYINT);
		types.put(Byte.class, OracleColumnType.TINYINT);
		types.put(short.class, OracleColumnType.SMALLINT);
		types.put(Short.class, OracleColumnType.SMALLINT);
		types.put(int.class, OracleColumnType.NUMBER);
		types.put(Integer.class, OracleColumnType.NUMBER);
		types.put(long.class, OracleColumnType.NUMBER);
		types.put(Long.class, OracleColumnType.NUMBER);
		types.put(float.class, OracleColumnType.FLOAT);
		types.put(Float.class, OracleColumnType.FLOAT);
		types.put(double.class, OracleColumnType.DOUBLE);
		types.put(Double.class, OracleColumnType.DOUBLE);
		types.put(byte[].class, OracleColumnType.VARBINARY);
		types.put(Date.class, OracleColumnType.DATETIME);
		types.put(java.util.Date.class, OracleColumnType.DATETIME);
		types.put(Calendar.class, OracleColumnType.DATETIME);
		types.put(Time.class, OracleColumnType.TIME);
		types.put(Timestamp.class, OracleColumnType.TIMESTAMP);
		types.put(Clob.class, OracleColumnType.CLOB);
		types.put(Blob.class, OracleColumnType.BLOB);
		types.put(Array.class, OracleColumnType.ARRAY);
		types.put(HeapByteBuf.class, OracleColumnType.BLOB);
	}
	
	@Override
	public ColumnType map(Class<?> type)
	{
		return types.get(type);
	}
	
	enum OracleColumnType implements ColumnType
	{
		/**
		 * Identifies the generic SQL type {@code BIT}.
		 */
		BIT("1"),
		/**
		 * Identifies the generic SQL type {@code TINYINT}.
		 */
		TINYINT(""),
		/**
		 * Identifies the generic SQL type {@code SMALLINT}.
		 */
		SMALLINT("5"),
		/**
		 * Identifies the generic SQL type {@code INTEGER}.
		 */
		INTEGER(""),
		/**
		 * Identifies the generic SQL type {@code BIGINT}.
		 */
		BIGINT("9"),
		/**
		 * Identifies the generic SQL type {@code FLOAT}.
		 */
		FLOAT(""),
		/**
		 * Identifies the generic SQL type {@code REAL}.
		 */
		REAL(""),
		/**
		 * Identifies the generic SQL type {@code DOUBLE}.
		 */
		DOUBLE(""),
		/**
		 * Identifies the generic SQL type {@code NUMERIC}.
		 */
		NUMERIC("9,2"), //
		NUMBER("22"),
		/**
		 * Identifies the generic SQL type {@code DECIMAL}.
		 */
		DECIMAL("9,2"),
		/**
		 * Identifies the generic SQL type {@code CHAR}.
		 */
		CHAR("255"),
		/**
		 * Identifies the generic SQL type {@code VARCHAR}.
		 */
		VARCHAR("255"),
		/**
		 * Identifies the generic SQL type {@code LONGVARCHAR}.
		 */
		LONGVARCHAR("255"),
		/**
		 * Identifies the generic SQL type {@code DATE}.
		 */
		DATE(""), //
		DATETIME(""),
		/**
		 * Identifies the generic SQL type {@code TIME}.
		 */
		TIME(""),
		/**
		 * Identifies the generic SQL type {@code TIMESTAMP}.
		 */
		TIMESTAMP(""),
		/**
		 * Identifies the generic SQL type {@code BINARY}.
		 */
		BINARY(""),
		/**
		 * Identifies the generic SQL type {@code VARBINARY}.
		 */
		VARBINARY(""),
		/**
		 * Identifies the generic SQL type {@code LONGVARBINARY}.
		 */
		LONGVARBINARY(""),
		/**
		 * Identifies the generic SQL value {@code NULL}.
		 */
		NULL(""),
		/**
		 * Indicates that the SQL type is database-specific and gets mapped to a
		 * Java object that can be accessed via the methods getObject and
		 * setObject.
		 */
		OTHER(""),
		/**
		 * Indicates that the SQL type is database-specific and gets mapped to a
		 * Java object that can be accessed via the methods getObject and
		 * setObject.
		 */
		JAVA_OBJECT(""),
		/**
		 * Identifies the generic SQL type {@code DISTINCT}.
		 */
		DISTINCT(""),
		/**
		 * Identifies the generic SQL type {@code STRUCT}.
		 */
		STRUCT(""),
		/**
		 * Identifies the generic SQL type {@code ARRAY}.
		 */
		ARRAY(""),
		/**
		 * Identifies the generic SQL type {@code BLOB}.
		 */
		BLOB(""),
		/**
		 * Identifies the generic SQL type {@code CLOB}.
		 */
		CLOB(""),
		/**
		 * Identifies the generic SQL type {@code REF}.
		 */
		REF(""),
		/**
		 * Identifies the generic SQL type {@code DATALINK}.
		 */
		DATALINK(""),
		/**
		 * Identifies the generic SQL type {@code BOOLEAN}.
		 */
		BOOLEAN("1"),
		
	    /* JDBC 4.0 Types */
		
		/**
		 * Identifies the SQL type {@code ROWID}.
		 */
		ROWID(""),
		/**
		 * Identifies the generic SQL type {@code NCHAR}.
		 */
		NCHAR(""),
		/**
		 * Identifies the generic SQL type {@code NVARCHAR}.
		 */
		NVARCHAR(""),
		/**
		 * Identifies the generic SQL type {@code LONGNVARCHAR}.
		 */
		LONGNVARCHAR(""),
		/**
		 * Identifies the generic SQL type {@code NCLOB}.
		 */
		NCLOB(""),
		/**
		 * Identifies the generic SQL type {@code SQLXML}.
		 */
		SQLXML(""),
		
	    /* JDBC 4.2 Types */
		
		/**
		 * Identifies the generic SQL type {@code REF_CURSOR}.
		 */
		REF_CURSOR("");
		
		final String desc;
		
		private OracleColumnType(String desc)
		{
			this.desc = desc;
		}
		
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
