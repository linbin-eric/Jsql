package com.jfireframework.sql.transfer.column.impl;

import java.lang.reflect.Field;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.transfer.column.ColumnTransfer;
import sun.misc.Unsafe;

public abstract class AbstractFieldOperator implements ColumnTransfer
{
    protected static final Unsafe unsafe = ReflectUtil.getUnsafe();
    protected long                offset;
    protected Field               field;
    
    @Override
    public void initialize(Field field)
    {
        this.field = field;
        offset = unsafe.objectFieldOffset(field);
    }
}
