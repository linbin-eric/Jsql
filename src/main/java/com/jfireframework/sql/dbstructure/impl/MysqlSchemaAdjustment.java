package com.jfireframework.sql.dbstructure.impl;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.StandardColumnDef;
import com.jfireframework.sql.annotation.TableDef;
import com.jfireframework.sql.annotation.pkstrategy.AutoIncrement;
import com.jfireframework.sql.dbstructure.Constraint;
import com.jfireframework.sql.dbstructure.Index;
import com.jfireframework.sql.dbstructure.SchemaAdjustment;
import com.jfireframework.sql.metadata.TableEntityInfo;
import com.jfireframework.sql.metadata.TableEntityInfo.ColumnInfo;
import com.jfireframework.sql.metadata.TableMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class MysqlSchemaAdjustment implements SchemaAdjustment
{
    private String findDatabase = "select database()";
    private String findTable = "SELECT count(*) from information_schema.TABLES where TABLE_SCHEMA=? and TABLE_NAME=?";
    private String findColumn = "SELECT COLUMN_NAME,DATA_TYPE,IS_NULLABLE,CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION,NUMERIC_SCALE,COLUMN_COMMENT from information_schema.`COLUMNS` where TABLE_SCHEMA=? and TABLE_NAME=? and COLUMN_NAME=?";
    private String addColumn = "ALTER TABLE {}.{} add {}  ";
    private String findColumnNames = "SELECT COLUMN_NAME from information_schema.`COLUMNS` where TABLE_SCHEMA=? and TABLE_NAME=?";
    private String findIndexs = "SHOW INDEX FROM ";
    private String addIndex = "CREATE INDEX {} USING {} ON {}.{} ({}) ;";
    private String dropIndex = "ALTER TABLE {}.{} DROP INDEX `{}`";
    private static final Logger logger = LoggerFactory.getLogger(MysqlSchemaAdjustment.class);

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
        TableDef tableDef = entityClass.getAnnotation(TableDef.class);
        String tableName = tableDef.name();
        dropTableIfExist(connection, tableName);
        createTable(connection, tableEntityInfo, tableDef, tableName);
    }

    private void createTable(Connection connection, TableEntityInfo info, TableDef tableDef, String tableName) throws SQLException
    {
        String createTableSql = generateCreateTableSql(info, tableDef, tableName);
        logger.debug("traceId:{} 生成的建表语句是:{}", TRACEID.currentTraceId(), createTableSql);
        PreparedStatement prepareStatement = connection.prepareStatement(createTableSql);
        prepareStatement.executeUpdate();
        prepareStatement.close();
    }

    private String generateCreateTableSql(TableEntityInfo each, TableDef tableDef, String tableName)
    {
        StringCache cache = new StringCache();
        cache.append("CREATE TABLE `").append(tableName).append("` (");
        createBody(each, cache);
        for (ColumnInfo info : each.getPropertyNameKeyMap().values())
        {
            String columnName = info.getColumnName();
            if ( info.getField().isAnnotationPresent(Constraint.class) )
            {
                setConstraint(cache, info.getField(), columnName);
            }
            if ( info.getField().isAnnotationPresent(Index.class) )
            {
                setIndex(cache, info.getField(), columnName);
            }
        }
        if ( cache.isCommaMeaningfulLast() != -1 )
        {
            cache.delete(cache.isCommaMeaningfulLast());
        }
        cache.append(") ENGINE = InnoDB DEFAULT CHARSET = utf8 ");
        if ( StringUtil.isNotBlank(tableDef.comment()) )
        {
            cache.append("comment=`").append(tableDef.comment()).append("`");
        }
        return cache.toString();
    }

    private void setIndex(StringCache cache, Field field, String columnName)
    {
        Index index = field.getAnnotation(Index.class);
        String indexName = StringUtil.isNotBlank(index.indexName()) ? index.indexName() : columnName + "_idx_" + count.getAndIncrement();
        if ( index.unique() )
        {
            cache.append("UNIQUE ");
        }
        cache.append("KEY `").append(indexName).append("` (`").append(columnName).append("`) USING ").append(index.indexType()).append(",\r\n");
    }

    private void setConstraint(StringCache cache, Field field, String columnName)
    {
        Constraint constraints = field.getAnnotation(Constraint.class);
        switch (constraints.type())
        {
            case PRIMARY_KEY:
                cache.append("PRIMARY KEY (`").append(columnName).append("`),\r\n");
                break;
            case UNIQUE_KEY:
                String constraintName = StringUtil.isNotBlank(constraints.name()) ? constraints.name() : columnName + "_uni_" + count.getAndIncrement();
                cache.append("UNIQUE KEY `").append(constraintName).append("` (`").append(columnName).append("`),\r\n");
                break;
            default:
                break;
        }
    }

    private void createBody(TableEntityInfo each, StringCache cache)
    {
        for (ColumnInfo columnInfo : each.getPropertyNameKeyMap().values())
        {
            StandardColumnDef mysqlColumnDef = columnInfo.getField().getAnnotation(StandardColumnDef.class);
            cache.append('`').append(columnInfo.getColumnName()).append("` ");
            String columnType = decideColumnType(columnInfo.getField(), mysqlColumnDef);
            cache.append(columnType).append(' ');
            boolean isNullable = mysqlColumnDef != null ? mysqlColumnDef.isNullable() : true;
            if ( isNullable == false )
            {
                cache.append("NOT NULL ");
            }
            else
            {
                cache.append("NULL ");
            }
            if ( columnInfo.getField().isAnnotationPresent(AutoIncrement.class) )
            {
                cache.append("AUTO_INCREMENT ");
            }
            if ( mysqlColumnDef != null && StringUtil.isNotBlank(mysqlColumnDef.comment()) )
            {
                cache.append(" COMMENT '").append(mysqlColumnDef.comment()).append('\'');
            }
            cache.append(",\r\n");
        }
    }

    private String decideColumnType(Field field, StandardColumnDef standardColumnDef)
    {
        String columnType;
        if ( standardColumnDef != null && StringUtil.isNotBlank(standardColumnDef.dataType()) )
        {
            String dataType = standardColumnDef.dataType();
            if ( "varchar".equals(dataType) )
            {
                columnType = "varchar(" + standardColumnDef.maxCharacterLength() + ")";
            }
            else if ( "float".equals(dataType) || "double".equals(dataType) )
            {
                columnType = dataType + "(" + standardColumnDef.numeric_precision() + "," + standardColumnDef.numeric_scale() + ")";
            }
            else if ( "datetime".equals(dataType) || "timestamp".equals(dataType) )
            {
                columnType = dataType + "(" + standardColumnDef.datetime_precision() + ")";
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
                columnType = "varchar(64)";
            }
            else if ( type == Integer.class || type == int.class || type == short.class || type == Short.class || type == byte.class || type == Byte.class )
            {
                columnType = "int";
            }
            else if ( type == boolean.class || type == Boolean.class )
            {
                columnType = "tinyint";
            }
            else if ( type == long.class || type == Long.class )
            {
                columnType = "bigint";
            }
            else if ( type == float.class || type == Float.class || type == Double.class || type == double.class )
            {
                columnType = "double";
            }
            else if ( type == Date.class )
            {
                columnType = "timestamp";
            }
            else if ( type == Time.class )
            {
                columnType = "time";
            }
            else if ( type == java.util.Date.class || type == Timestamp.class || type == Calendar.class )
            {
                columnType = "timestamp(3)";
            }
            else if ( type == Clob.class )
            {
                columnType = "text";
            }
            else if ( type == Blob.class )
            {
                columnType = "blob";
            }
            else if ( type == byte[].class )
            {
                columnType = "blob";
            }
            else
            {
                throw new UnsupportedOperationException("不支持的自动映射类型，请为属性" + field.getDeclaringClass().getName() + "." + field.getName() + "增加类定义注解");
            }
        }
        return columnType;
    }

    private void dropTableIfExist(Connection connection, String tableName) throws SQLException
    {
        PreparedStatement prepareStatement = connection.prepareStatement("DROP TABLE IF EXISTS " + tableName);
        prepareStatement.executeUpdate();
        prepareStatement.close();
    }

    private void updateTable(DataSource dataSource, Set<TableEntityInfo> tableEntityInfos) throws SQLException
    {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        String schema = getSchema(connection);
        for (TableEntityInfo each : tableEntityInfos)
        {
            Class<?> entityClass = each.getEntityClass();
            if ( entityClass.isAnnotationPresent(TableDef.class) == false )
            {
                continue;
            }
            if ( isTableExist(connection, schema, entityClass.getAnnotation(TableDef.class).name()) == false )
            {
                _createTable(each, connection);
                continue;
            }
            String tableName = entityClass.getAnnotation(TableDef.class).name();
            addMissingColumns(connection, schema, each, tableName);
            dropNotExistColumns(connection, schema, tableName, each);
            Map<String, String> indexs = getIndexColumnNames(connection, tableName);
            addMissingIndex(connection, schema, each, tableName, indexs);
            dropNotExistIndexs(connection, schema, tableName, indexs);
        }
        connection.commit();
        connection.setAutoCommit(true);
        connection.close();
    }

    /**
     * 添加表中还不存在的索引。如果该索引已经存在于表中，则从map中删除该数据
     *
     * @param connection
     * @param schema
     * @param each
     * @param tableName
     * @param propertyNameToColumnNameMap
     * @param indexs
     * @throws SQLException
     */
    private void addMissingIndex(Connection connection, String schema, TableEntityInfo each, String tableName, Map<String, String> indexs) throws SQLException
    {
        for (ColumnInfo info : each.getPropertyNameKeyMap().values())
        {
            if ( info.getField().isAnnotationPresent(Index.class) )
            {
                String columnName = info.getColumnName();
                if ( indexs.containsKey(columnName) )
                {
                    indexs.remove(columnName);
                }
                else
                {
                    addIndex(connection, schema, tableName, info.getField(), columnName);
                }
            }
        }
    }

    private void dropNotExistIndexs(Connection connection, String schema, String tableName, Map<String, String> indexs) throws SQLException
    {
        if ( indexs.isEmpty() == false )
        {
            for (String indexName : indexs.values())
            {
                String dropIndexSql = StringUtil.format(dropIndex, schema, tableName, indexName);
                logger.debug("traceId:{} 准备删除索引:{}", TRACEID.currentTraceId(), dropIndexSql);
                PreparedStatement prepareStatement = connection.prepareStatement(dropIndexSql);
                prepareStatement.executeUpdate();
                prepareStatement.close();
            }
        }
    }

    private void addIndex(Connection connection, String schema, String tableName, Field field, String columnName) throws SQLException
    {
        Index index = field.getAnnotation(Index.class);
        String indexName = StringUtil.isNotBlank(index.indexName()) ? index.indexName() : columnName + "_idx_" + count.getAndIncrement();
        String indexType = index.indexType();
        String addIndexSql = StringUtil.format(addIndex, indexName, indexType, schema, tableName, columnName);
        PreparedStatement prepareStatement = connection.prepareStatement(addIndexSql);
        prepareStatement.executeUpdate();
        prepareStatement.close();
    }

    /**
     * 返回索引信息。key为列名，value为索引名称
     *
     * @param connection
     * @param tableName
     * @return
     * @throws SQLException
     */
    private Map<String, String> getIndexColumnNames(Connection connection, String tableName) throws SQLException
    {
        String showIndexSql = findIndexs + tableName;
        PreparedStatement prepareStatement = connection.prepareStatement(showIndexSql);
        ResultSet resultSet = prepareStatement.executeQuery();
        Map<String, String> indexs = new HashMap<String, String>();
        while (resultSet.next())
        {
            indexs.put(resultSet.getString("Column_name"), resultSet.getString("Key_name"));
        }
        resultSet.close();
        prepareStatement.close();
        return indexs;
    }

    private void dropNotExistColumns(Connection connection, String schema, String tableName, TableEntityInfo info) throws SQLException
    {
        PreparedStatement prepareStatement = connection.prepareStatement(findColumnNames);
        prepareStatement.setString(1, schema);
        prepareStatement.setString(2, tableName);
        ResultSet resultSet = prepareStatement.executeQuery();
        List<String> notExistColumnNames = new LinkedList<String>();
        while (resultSet.next())
        {
            String columnName = resultSet.getString(1);
            if ( info.getColumnInfoByColumnNameIgnoreCase(columnName) == null )
            {
                notExistColumnNames.add(columnName);
            }
        }
        resultSet.close();
        prepareStatement.close();
        String dropColumnSqlPrefix = "ALTER TABLE " + tableName + " DROP COLUMN ";
        for (String notExistColumnName : notExistColumnNames)
        {
            PreparedStatement prepareStatement2 = connection.prepareStatement(dropColumnSqlPrefix + notExistColumnName);
            prepareStatement2.execute();
            prepareStatement2.close();
        }
    }

    private void addMissingColumns(Connection connection, String schema, TableEntityInfo each, String tableName) throws SQLException
    {
        for (ColumnInfo columnInfo : each.getPropertyNameKeyMap().values())
        {
            StandardColumnDef columnDef = columnInfo.getField().getAnnotation(StandardColumnDef.class);
            String columnName = columnInfo.getColumnName();
            ColumnDefinition columnDefinition = queryColumnInfo(connection, schema, tableName, columnName);
            if ( columnDefinition == null )
            {
                addColumn(connection, schema, tableName, columnInfo.getField(), columnDef, columnName);
            }
            else
            {
                String columnType = decideColumnType(columnInfo.getField(), columnDef);
                if ( columnType.toLowerCase().startsWith(columnDefinition.dataType.toLowerCase()) == false )
                {
                    String dropColumnSqlPrefix = "ALTER TABLE " + tableName + " DROP COLUMN ";
                    PreparedStatement ps = connection.prepareStatement(dropColumnSqlPrefix + columnDefinition.columnName);
                    ps.execute();
                    ps.close();
                    addColumn(connection, schema, tableName, columnInfo.getField(), columnDef, columnName);
                }
            }
        }
    }

    private void addColumn(Connection connection, String schema, String tableName, Field field, StandardColumnDef columnDef, String columnName) throws SQLException
    {
        StringCache cache = new StringCache(StringUtil.format(addColumn, schema, tableName, columnName));
        String columnType = decideColumnType(field, columnDef);
        cache.append(columnType).append(' ');
        boolean isNullable = columnDef != null ? columnDef.isNullable() : false;
        if ( isNullable == false )
        {
            cache.append("NOT NULL ");
        }
        if ( field.isAnnotationPresent(AutoIncrement.class) )
        {
            cache.append("AUTO_INCREMENT ");
        }
        if ( columnDef != null && StringUtil.isNotBlank(columnDef.comment()) )
        {
            cache.append(" COMMENT '").append(columnDef.comment()).append('\'');
        }
        String addColumnSql = cache.toString();
        PreparedStatement prepareStatement = connection.prepareStatement(addColumnSql);
        prepareStatement.executeUpdate();
        prepareStatement.close();
    }

    class ColumnDefinition
    {
        String columnName;
        String dataType;
        boolean isNullable;
        Integer maxCharactersLength;
        String comment;
        int numeric_precision;
        int numeric_scale;
    }

    private ColumnDefinition queryColumnInfo(Connection connection, String schema, String tableName, String columnName) throws SQLException
    {
        PreparedStatement prepareStatement = connection.prepareStatement(findColumn);
        prepareStatement.setString(1, schema);
        prepareStatement.setString(2, tableName);
        prepareStatement.setString(3, columnName);
        ResultSet resultSet = prepareStatement.executeQuery();
        if ( resultSet.next() == false )
        {
            return null;
        }
        else
        {
            ColumnDefinition columnInfo = new ColumnDefinition();
            columnInfo.columnName = resultSet.getString("COLUMN_NAME");
            columnInfo.dataType = resultSet.getString("DATA_TYPE");
            columnInfo.isNullable = resultSet.getString("IS_NULLABLE").equals("YES");
            int CHARACTER_MAXIMUM_LENGTH = resultSet.getInt("CHARACTER_MAXIMUM_LENGTH");
            columnInfo.maxCharactersLength = resultSet.wasNull() ? null : CHARACTER_MAXIMUM_LENGTH;
            int NUMERIC_PRECISION = resultSet.getInt("NUMERIC_PRECISION");
            columnInfo.numeric_precision = resultSet.wasNull() ? null : NUMERIC_PRECISION;
            int NUMERIC_SCALE = resultSet.getInt("NUMERIC_SCALE");
            columnInfo.numeric_scale = resultSet.wasNull() ? null : NUMERIC_SCALE;
            columnInfo.comment = resultSet.getString("COLUMN_COMMENT");
            return columnInfo;
        }
    }

    private boolean isTableExist(Connection connection, String schema, String tableName) throws SQLException
    {
        PreparedStatement prepareStatement = connection.prepareStatement(findTable);
        prepareStatement.setString(1, schema);
        prepareStatement.setString(2, tableName);
        ResultSet resultSet = prepareStatement.executeQuery();
        resultSet.next();
        int count = resultSet.getInt(1);
        return count == 1;
    }

    private String getSchema(Connection connection) throws SQLException
    {
        PreparedStatement prepareStatement = connection.prepareStatement(findDatabase);
        ResultSet resultSet = prepareStatement.executeQuery();
        resultSet.next();
        String schema = resultSet.getString(1);
        return schema;
    }

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
                updateTable(dataSource, tableEntityInfos);
                break;
            default:
                break;
        }
    }

}
