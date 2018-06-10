package com.jfireframework.sql.dbstructure.impl;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.ColumnDef;
import com.jfireframework.sql.annotation.TableDef;
import com.jfireframework.sql.annotation.pkstrategy.AutoIncrement;
import com.jfireframework.sql.dbstructure.SchemaAdjustment;
import com.jfireframework.sql.util.TableEntityInfo;
import com.jfireframework.sql.util.TableMode;

public class H2SchemaAdjustment implements SchemaAdjustment
{
    private String              dropTable = "DROP TABLE IF EXISTS ";
    private static final Logger logger    = LoggerFactory.getLogger(H2SchemaAdjustment.class);
    
    @Override
    public void adjust(TableMode mode, DataSource dataSource, Set<TableEntityInfo> tableEntityInfos) throws SQLException
    {
        switch (mode)
        {
            case NONE:
                break;
            case CREATE:
                createTable(dataSource, tableEntityInfos);
                break;
            case UPDATE:
                break;
            default:
                break;
        }
    }
    
    protected void createTable(DataSource dataSource, Set<TableEntityInfo> tableEntityInfos) throws SQLException
    {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        for (TableEntityInfo each : tableEntityInfos)
        {
            _createTable(each, connection);
        }
        connection.commit();
        connection.setAutoCommit(true);
        connection.close();
    }
    
    private void _createTable(TableEntityInfo tableEntityInfo, Connection connection) throws SQLException
    {
        Class<?> entityClass = tableEntityInfo.getEntityClass();
        Map<String, String> propertyNameToColumnNameMap = tableEntityInfo.getPropertyNameToColumnNameMap();
        TableDef tableDef = entityClass.getAnnotation(TableDef.class);
        String tableName = tableDef.name();
        dropTableIfExist(connection, tableName);
        createTable(connection, tableEntityInfo, propertyNameToColumnNameMap, tableDef, tableName);
    }
    
    private void dropTableIfExist(Connection connection, String tableName) throws SQLException
    {
        PreparedStatement prepareStatement = connection.prepareStatement(dropTable + tableName);
        prepareStatement.executeUpdate();
        prepareStatement.close();
    }
    
    private void createTable(Connection connection, TableEntityInfo info, Map<String, String> propertyNameToColumnNameMap, TableDef tableDef, String tableName) throws SQLException
    {
        String createTableSql = generateCreateTableSql(info, propertyNameToColumnNameMap, tableDef, tableName);
        logger.debug("traceId:{} 生成的建表语句是:{}", TRACEID.currentTraceId(), createTableSql);
        PreparedStatement prepareStatement = connection.prepareStatement(createTableSql);
        prepareStatement.executeUpdate();
        prepareStatement.close();
    }
    
    private String generateCreateTableSql(TableEntityInfo info, Map<String, String> propertyNameToColumnNameMap, TableDef tableDef, String tableName)
    {
        StringCache cache = new StringCache();
        cache.append("CREATE TABLE PUBLIC.").append(info.getTableName()).append(" (\r\n");
        for (Entry<String, Field> entry : info.getColumnNameToFieldMap().entrySet())
        {
            ColumnDef columnDef = entry.getValue().getAnnotation(ColumnDef.class);
            String columnName = entry.getKey();
            String columnType = decideColumnType(entry.getValue(), columnDef);
            cache.append(columnName).append(' ').append(columnType).append(' ');
            if (columnDef != null && columnDef.isNullable() == false)
            {
                cache.append("NOT NULL ");
            }
            if (entry.getValue().isAnnotationPresent(AutoIncrement.class))
            {
                cache.append("AUTO_INCREMENT");
            }
            cache.append(",\r\n");
        }
        int commaIndex = cache.isCommaMeaningfulLast();
        cache.delete(commaIndex);
        cache.append(")");
        return cache.toString();
    }
    
    private String decideColumnType(Field field, ColumnDef columnDef)
    {
        String columnType;
        if (columnDef != null && StringUtil.isNotBlank(columnDef.dataType()))
        {
            String dataType = columnDef.dataType();
            if ("varchar".equals(dataType))
            {
                columnType = "varchar(" + columnDef.maxCharacterLength() + ")";
            }
            else
            {
                columnType = dataType;
            }
        }
        else
        {
            Class<?> type = field.getType();
            if (type == String.class)
            {
                columnType = "VARCHAR(64)";
            }
            else if (type == Integer.class || type == int.class || type == short.class || type == Short.class || type == byte.class || type == Byte.class)
            {
                columnType = "INTEGER";
            }
            else if (type == boolean.class || type == Boolean.class)
            {
                columnType = "BOOLEAN";
            }
            else if (type == long.class || type == Long.class)
            {
                columnType = "BIGINT";
            }
            else if (type == float.class || type == Float.class || type == Double.class || type == double.class)
            {
                columnType = "DOUBLE";
            }
            else if (type == Date.class || type == java.util.Date.class || type == Timestamp.class || type == Calendar.class || type == Time.class)
            {
                columnType = "TIMESTAMP";
            }
            else if (type == Clob.class)
            {
                columnType = "CLOB";
            }
            else if (type == Blob.class)
            {
                columnType = "BLOB";
            }
            else if (type == byte[].class)
            {
                columnType = "BLOB";
            }
            else
            {
                throw new UnsupportedOperationException("不支持的自动映射类型，请为属性" + field.getDeclaringClass().getName() + "." + field.getName() + "增加类定义注解");
            }
        }
        return columnType;
    }
}
