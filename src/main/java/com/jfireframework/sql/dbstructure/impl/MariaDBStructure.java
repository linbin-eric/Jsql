package com.jfireframework.sql.dbstructure.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.metadata.TableMetaData;
import sun.print.resources.serviceui;

public class MariaDBStructure extends AbstractDBStructure
{
	
	public MariaDBStructure(String schema)
	{
		super(schema);
	}
	
	@Override
	protected void _createTable(Connection connection, TableMetaData tableMetaData) throws SQLException
	{
		String tableName = tableMetaData.getTableName();
		MapField idInfo = tableMetaData.getIdInfo();
		StringCache cache = new StringCache();
		cache.append("CREATE TABLE ").append(tableName).append(" (");
		cache.append(idInfo.getColName()).append(' ').append(getDesc(idInfo, tableMetaData));
		if (idInfo.getField().getType() == Integer.class || idInfo.getField().getType() == Long.class)
		{
			cache.append(" AUTO_INCREMENT ");
		}
		cache.append(" primary key").appendComma();
		for (MapField each : tableMetaData.getFieldInfos())
		{
			if (each.getFieldName().equals(idInfo.getFieldName()))
			{
				continue;
			}
			try
			{
				cache.append(each.getColName()).append(' ').append(getDesc(each, tableMetaData)).appendComma();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		cache.deleteLast().append(")");
		logger.warn("进行表:{}的创建，创建语句是\n{}", tableName, cache.toString());
		connection.prepareStatement("DROP TABLE IF EXISTS " + tableName).execute();
		connection.prepareStatement(cache.toString()).execute();
	}
	
	protected void _updateTable(Connection connection, TableMetaData tableMetaData) throws SQLException
	{
		String tableName = tableMetaData.getTableName();
		String addColSql = "alter table " + tableName + " add ";
		String describeSql = "describe " + tableName + ' ';
		String modityColSql = "alter table " + tableName + " modify ";
		connection.prepareStatement("describe " + tableName).execute();
		MapField idInfo = tableMetaData.getIdInfo();
		ResultSet rs = connection.prepareStatement("describe " + tableName + " " + idInfo.getColName()).executeQuery();
		if (rs.next())
		{
			// 字段存在，需要执行更新操作
			if (idInfo.getField().getType() == Integer.class || idInfo.getField().getType() == Long.class)
			{
				logger.debug("执行sql语句:{}", "alter table " + tableName + " modify " + idInfo.getColName() + ' ' + getDesc(idInfo, tableMetaData) + " auto_increment");
				connection.prepareStatement("alter table " + tableName + " modify " + idInfo.getColName() + ' ' + getDesc(idInfo, tableMetaData) + " auto_increment").execute();
			}
			else
			{
				logger.debug("执行sql语句:{}", "alter table " + tableName + " modify " + idInfo.getColName() + ' ' + getDesc(idInfo, tableMetaData));
				connection.prepareStatement("alter table " + tableName + " modify " + idInfo.getColName() + ' ' + getDesc(idInfo, tableMetaData)).execute();
			}
		}
		else
		{
			// 字段不存在，需要执行新建动作
			if (idInfo.getField().getType() == Integer.class || idInfo.getField().getType() == Long.class)
			{
				logger.warn("执行sql语句:{}", addColSql + idInfo.getColName() + ' ' + getDesc(idInfo, tableMetaData) + " auto_increment");
				connection.prepareStatement(addColSql + idInfo.getColName() + ' ' + getDesc(idInfo, tableMetaData) + " auto_increment").execute();
			}
			else
			{
				logger.warn("执行sql语句:{}", addColSql + idInfo.getColName() + ' ' + getDesc(idInfo, tableMetaData));
				connection.prepareStatement(addColSql + idInfo.getColName() + ' ' + getDesc(idInfo, tableMetaData)).execute();
			}
		}
		rs.close();
		for (MapField each : tableMetaData.getFieldInfos())
		{
			if (each.getColName().equals(idInfo.getColName()))
			{
				continue;
			}
			rs = connection.prepareStatement(describeSql + each.getColName()).executeQuery();
			if (rs.next())
			{
				logger.debug("执行sql语句:{}", modityColSql + each.getColName() + ' ' + getDesc(each, tableMetaData));
				connection.prepareStatement(modityColSql + each.getColName() + ' ' + getDesc(each, tableMetaData)).execute();
			}
			else
			{
				logger.debug("执行sql语句:{}", addColSql + each.getColName() + ' ' + getDesc(each, tableMetaData));
				connection.prepareStatement(addColSql + each.getColName() + ' ' + getDesc(each, tableMetaData)).execute();
			}
			rs.close();
		}
	}
	
	@Override
	protected boolean checkIfTableExists(Connection connection, TableMetaData metaData) throws SQLException
	{
		ResultSet executeQuery = connection.prepareStatement(StringUtil.format("select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME='{}' and TABLE_SCHEMA='{}'", metaData.getTableName(), schema)).executeQuery();
		return executeQuery.next();
	}
	
}
