package com.jfireframework.sql.transfer.column.impl;

import java.lang.reflect.Field;
import com.jfireframework.sql.transfer.column.ColumnTransfer;

public abstract class AbstractColumnTransfer implements ColumnTransfer
{
	protected Field		field;
	protected String	columnName;
	
	@Override
	public void initialize(Field field, String columnName)
	{
		this.field = field;
		this.columnName = columnName;
	}
}
