package com.jfirer.jsql.dbstructure.impl;

import com.jfirer.jsql.annotation.StandardColumnDef;
import com.jfirer.jsql.annotation.TableDef;
import com.jfirer.jsql.annotation.pkstrategy.AutoIncrement;
import com.jfirer.jsql.dbstructure.SchemaAdjustment;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.metadata.TableMode;
import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.TRACEID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.Calendar;
import java.util.Set;

public class H2SchemaAdjustment implements SchemaAdjustment
{
    private static final Logger logger = LoggerFactory.getLogger(H2SchemaAdjustment.class);

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
                createTable(dataSource, tableEntityInfos);
                break;
            default:
                break;
        }
    }

    private void createTable(DataSource dataSource, Set<TableEntityInfo> tableEntityInfos) throws SQLException
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
        TableDef tableDef    = entityClass.getAnnotation(TableDef.class);
        String   tableName   = tableDef.name();
        dropTableIfExist(connection, tableName);
        createTable(connection, tableEntityInfo, tableDef, tableName);
    }

    private void dropTableIfExist(Connection connection, String tableName) throws SQLException
    {
        String dropTable = "DROP TABLE IF EXISTS ";
        PreparedStatement prepareStatement = connection.prepareStatement(dropTable + tableName);
        prepareStatement.executeUpdate();
        prepareStatement.close();
    }

    private void createTable(Connection connection, TableEntityInfo info, TableDef tableDef, String tableName) throws SQLException
    {
        String createTableSql = generateCreateTableSql(info, tableDef, tableName);
        logger.debug("traceId:{} 生成的建表语句是:{}", TRACEID.currentTraceId(), createTableSql);
        PreparedStatement prepareStatement = connection.prepareStatement(createTableSql);
        prepareStatement.executeUpdate();
        prepareStatement.close();
    }

    private String generateCreateTableSql(TableEntityInfo info, TableDef tableDef, String tableName)
    {
        StringBuilder cache = new StringBuilder();
        cache.append("CREATE TABLE PUBLIC.").append(info.getTableName()).append(" (\r\n");
        for (TableEntityInfo.ColumnInfo columnInfo : info.getPropertyNameKeyMap().values())
        {
            StandardColumnDef columnDef  = columnInfo.getField().getAnnotation(StandardColumnDef.class);
            String            columnName = columnInfo.getColumnName();
            String            columnType = decideColumnType(columnInfo.getField(), columnDef);
            cache.append(columnName).append(' ').append(columnType).append(' ');
            if ( columnDef != null && columnDef.isNullable() == false )
            {
                cache.append("NOT NULL ");
            }
            if ( columnInfo.getField().isAnnotationPresent(AutoIncrement.class) )
            {
                cache.append("AUTO_INCREMENT");
            }
            cache.append(",\r\n");
        }
        cache.deleteCharAt(cache.length()-3);
        cache.append(")");
        return cache.toString();
    }

    private String decideColumnType(Field field, StandardColumnDef columnDef)
    {
        String columnType;
        if ( columnDef != null && StringUtil.isNotBlank(columnDef.dataType()) )
        {
            String dataType = columnDef.dataType();
            if ( "varchar".equals(dataType) )
            {
                columnType = "varchar(" + columnDef.maxCharacterLength() + ")";
            }
            else if ( "datetime".equals(dataType) || "timestamp".equals(dataType) )
            {
                columnType = dataType + "(" + columnDef.datetime_precision() + ")";
            }
            else
            {
                columnType = dataType;
            }
        }
        else
        {
            Class<?> type = field.getType();
            if ( type == String.class )
            {
                columnType = "VARCHAR(64)";
            }
            else if ( type == Integer.class || type == int.class || type == short.class || type == Short.class || type == byte.class || type == Byte.class )
            {
                columnType = "INTEGER";
            }
            else if ( type == boolean.class || type == Boolean.class )
            {
                columnType = "BOOLEAN";
            }
            else if ( type == long.class || type == Long.class )
            {
                columnType = "BIGINT";
            }
            else if ( type == float.class || type == Float.class || type == Double.class || type == double.class )
            {
                columnType = "DOUBLE";
            }
            else if ( type == Date.class )
            {
                columnType = "TIMESTAMP";
            }
            else if ( type == Time.class )
            {
                columnType = "TIME";
            }
            else if ( type == java.util.Date.class || type == Timestamp.class || type == Calendar.class )
            {
                columnType = "TIMESTAMP(3)";
            }
            else if ( type == Clob.class )
            {
                columnType = "CLOB";
            }
            else if ( type == Blob.class )
            {
                columnType = "BLOB";
            }
            else if ( type == byte[].class )
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
