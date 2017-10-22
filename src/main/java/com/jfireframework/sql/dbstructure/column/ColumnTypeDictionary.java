package com.jfireframework.sql.dbstructure.column;

public interface ColumnTypeDictionary
{
    ColumnType map(Class<?> type);
}
