package com.jfireframework.sql.dbstructure.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.dbstructure.Structure;
import com.jfireframework.sql.dbstructure.TableDef;
import com.jfireframework.sql.dbstructure.column.MysqlColumnDef;
import com.jfireframework.sql.util.TableEntityInfo;

public class MysqlStructure implements Structure
{
	private String findDatabase = "select database()";
	
	@Override
	public void createTable(DataSource dataSource, Set<TableEntityInfo> tableEntityInfos) throws SQLException
	{
		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(false);
		for (TableEntityInfo each : tableEntityInfos)
		{
			Class<?> entityClass = each.getEntityClass();
			Map<String, String> propertyNameToColumnNameMap = each.getPropertyNameToColumnNameMap();
			if (entityClass.isAnnotationPresent(TableDef.class) == false)
			{
				continue;
			}
			TableDef tableDef = entityClass.getAnnotation(TableDef.class);
			String tableName = tableDef.value();
			connection.prepareStatement("DROP TABLE IF EXISTS " + tableName);
			StringCache cache = new StringCache();
			cache.append("CREATE TABLE '").append(tableName).append("' (\r\n");
			for (Field field : each.getColumnNameToFieldMap().values())
			{
				if (field.isAnnotationPresent(MysqlColumnDef.class))
				{
					MysqlColumnDef mysqlColumnDef = field.getAnnotation(MysqlColumnDef.class);
					String columnName = StringUtil.isNotBlank(mysqlColumnDef.columnName()) ? mysqlColumnDef.columnName() : propertyNameToColumnNameMap.get(field.getName());
					cache.append('\'').append(columnName).append("' ");
					if (StringUtil.isNotBlank(mysqlColumnDef.dataType()))
					{
						
					}
					else
					{
						
					}
				}
				else
				{
					
				}
			}
			
		}
	}
	
	@Override
	public void updateTable(DataSource dataSource, Set<TableEntityInfo> tableEntityInfos) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	
}
