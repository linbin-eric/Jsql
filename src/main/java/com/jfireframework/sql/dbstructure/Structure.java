package com.jfireframework.sql.dbstructure;

import java.sql.SQLException;
import java.util.Set;
import javax.sql.DataSource;
import com.jfireframework.sql.util.TableEntityInfo;

public interface Structure
{
	void createTable(DataSource dataSource, Set<TableEntityInfo> tableEntityInfos) throws SQLException;
	
	void updateTable(DataSource dataSource, Set<TableEntityInfo> tableEntityInfos) throws SQLException;
}
