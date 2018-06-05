package com.jfireframework.sql.dialect;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface Dialect
{
	/**
	 * 填充参数到preparedStatement中
	 * 
	 * @param preparedStatement
	 * @param params
	 * @throws SQLException
	 */
	void fillStatement(PreparedStatement preparedStatement, List<Object> params) throws SQLException;
}
