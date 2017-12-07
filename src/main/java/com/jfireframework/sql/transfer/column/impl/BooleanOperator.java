package com.jfireframework.sql.transfer.column.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BooleanOperator extends AbstractFieldOperator
{
	
	@Override
	public void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException
	{
		boolean value = resultSet.getBoolean(dbColName);
		if (resultSet.wasNull() == false)
		{
			unsafe.putBoolean(entity, offset, value);
		}
	}
	
}
