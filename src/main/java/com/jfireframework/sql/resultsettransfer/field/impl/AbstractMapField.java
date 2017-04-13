package com.jfireframework.sql.resultsettransfer.field.impl;

import java.lang.reflect.Field;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.Column;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;
import com.jfireframework.sql.resultsettransfer.field.MapField;
import sun.misc.Unsafe;

/**
 * 基础CURD操作映射的抽象属性类
 * 
 * @author linbin
 * 
 */
public abstract class AbstractMapField implements MapField
{
    protected final long          offset;
    protected final String        dbColName;
    protected final static Unsafe unsafe = ReflectUtil.getUnsafe();
    protected final boolean       saveIgnore;
    protected boolean             loadIgnore;
    protected final Field         field;
    protected int                 length;
    
    public AbstractMapField(Field field, ColNameStrategy colNameStrategy)
    {
        offset = unsafe.objectFieldOffset(field);
        this.field = field;
        if (field.isAnnotationPresent(Column.class))
        {
            Column column = field.getAnnotation(Column.class);
            if (StringUtil.isNotBlank(column.name()))
            {
                dbColName = field.getAnnotation(Column.class).name();
            }
            else
            {
                dbColName = colNameStrategy.toDbName(field.getName());
            }
            loadIgnore = column.loadIgnore();
            saveIgnore = column.saveIgnore();
            length = column.length();
        }
        else
        {
            length = -1;
            saveIgnore = false;
            loadIgnore = false;
            dbColName = colNameStrategy.toDbName(field.getName());
        }
    }
    
    @Override
    public String getColName()
    {
        return dbColName;
    }
    
    @Override
    public boolean saveIgnore()
    {
        return saveIgnore;
    }
    
    @Override
    public boolean loadIgnore()
    {
        return loadIgnore;
    }
    
    @Override
    public String getFieldName()
    {
        return field.getName();
    }
    
    @Override
    public Class<?> getFieldType()
    {
        return field.getType();
    }
    
    @Override
    public int getDbLength()
    {
        return length;
    }
    
    /**
     * 返回原始的field对象
     * 
     * @return
     */
    @Override
    public Field getField()
    {
        return field;
    }
    
    @Override
    public int hashCode()
    {
        return field.hashCode();
    }
    
    @Override
    public boolean equals(Object o)
    {
        return field.equals(o);
    }
    
    @Override
    public Object statementValue(Object entity)
    {
        return unsafe.getObject(entity, offset);
    }
}
