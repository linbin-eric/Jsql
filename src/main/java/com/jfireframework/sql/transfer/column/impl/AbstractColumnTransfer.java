package com.jfireframework.sql.transfer.column.impl;

import com.jfireframework.sql.transfer.column.ColumnTransfer;

import java.lang.reflect.Field;

public abstract class AbstractColumnTransfer implements ColumnTransfer
{
    protected Field field;
    protected String columnName;

    @Override
    public void initialize(Field field, String columnName)
    {
        this.field = field;
        this.columnName = columnName;
    }
}
