package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;

public abstract class ColumnIndexHolder implements ResultSetTransfer
{
    protected final int columnIndex;

    public ColumnIndexHolder(int columnIndex)
    {
        this.columnIndex = columnIndex;
    }

    @Override
    public ResultSetTransfer awareType(Class type) {return this;}
}
