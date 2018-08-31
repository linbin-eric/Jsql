package com.jfireframework.sql.transfer.column.impl;

import com.jfireframework.sql.transfer.column.ColumnTransfer;

import java.lang.reflect.Field;

public abstract class AbstractColumnTransfer implements ColumnTransfer
{
    Field field;
    String columnName;

    @Override
    public void initialize(Field field, String columnName)
    {
        this.field = field;
        this.columnName = columnName;
    }
}
