package com.jfirer.jsql.executor.impl;

import com.jfirer.jsql.executor.SqlExecutor;

public abstract class NextHolder implements SqlExecutor
{
    protected SqlExecutor next;

    @Override
    public void setNext(SqlExecutor next)
    {
        this.next = next;
    }
}
