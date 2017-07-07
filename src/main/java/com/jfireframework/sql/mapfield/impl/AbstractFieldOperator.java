package com.jfireframework.sql.mapfield.impl;

import java.lang.reflect.Field;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.mapfield.FieldOperator;
import sun.misc.Unsafe;

public abstract class AbstractFieldOperator implements FieldOperator
{
    protected static final Unsafe unsafe = ReflectUtil.getUnsafe();
    
    @Override
    public void initialize(Field field)
    {
        
    }
}
