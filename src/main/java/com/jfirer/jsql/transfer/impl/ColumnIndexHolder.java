package com.jfirer.jsql.transfer.impl;

import com.jfirer.jsql.transfer.ResultSetTransfer;

public abstract class ColumnIndexHolder implements ResultSetTransfer
{
    protected int columnIndex=1;

    public ColumnIndexHolder(int columnIndex)
    {
        this.columnIndex = columnIndex;
    }

    public ColumnIndexHolder()
    {
        columnIndex = 1;
    }

    @Override
    public ResultSetTransfer awareType(Class type) {return this;}
}
