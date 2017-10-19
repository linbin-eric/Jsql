package com.jfireframework.sql.dbstructure.column;

public enum StandandType implements ColumnType
{
	/**
	 * Identifies the generic SQL type {@code BIT}.
	 */
	BIT("1"),
	/**
	 * Identifies the generic SQL type {@code TINYINT}.
	 */
	TINYINT("2"),
	/**
	 * Identifies the generic SQL type {@code SMALLINT}.
	 */
	SMALLINT("5"),
	/**
	 * Identifies the generic SQL type {@code INTEGER}.
	 */
	INTEGER("9"),
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
	NUMERIC("9,2"),
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
	 * Java object that can be accessed via the methods getObject and setObject.
	 */
	OTHER(""),
	/**
	 * Indicates that the SQL type is database-specific and gets mapped to a
	 * Java object that can be accessed via the methods getObject and setObject.
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
	
	private StandandType(String desc)
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
