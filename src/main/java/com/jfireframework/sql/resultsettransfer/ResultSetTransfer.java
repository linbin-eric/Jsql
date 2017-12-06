package com.jfireframework.sql.resultsettransfer;

import java.sql.ResultSet;
import java.util.List;
import com.jfireframework.sql.SessionfactoryConfig;

public interface ResultSetTransfer<T>
{
	void initialize(Class<T> type, SessionfactoryConfig config);
	
	T transfer(ResultSet resultSet) throws Exception;
	
	List<T> transferList(ResultSet resultSet) throws Exception;
}
