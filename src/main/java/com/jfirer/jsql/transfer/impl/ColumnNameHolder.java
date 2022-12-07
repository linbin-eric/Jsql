package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;

public abstract class ColumnNameHolder implements ResultSetTransfer
{
    protected String columnName;

    public ColumnNameHolder(String columnName)
    {
        this.columnName = columnName;
    }

    public ColumnNameHolder()
    {
    }

    @Override
    public ResultSetTransfer awareType(Class type) {return this;}
}
