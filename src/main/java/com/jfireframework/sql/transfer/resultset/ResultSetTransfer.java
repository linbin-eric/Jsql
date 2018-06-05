package com.jfireframework.sql.transfer.resultset;

import java.sql.ResultSet;
import com.jfireframework.sql.SessionfactoryConfig;

public interface ResultSetTransfer
{
	void initialize(Class<?> type, SessionfactoryConfig config);
	
	Object transfer(ResultSet resultSet);
	
}
