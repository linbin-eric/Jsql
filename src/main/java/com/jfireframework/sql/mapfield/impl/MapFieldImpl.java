package com.jfireframework.sql.mapfield.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.Column;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;
import com.jfireframework.sql.mapfield.FieldOperator;
import com.jfireframework.sql.mapfield.FieldOperatorDictionary;
import com.jfireframework.sql.mapfield.FieldOperatorUtil;
import com.jfireframework.sql.mapfield.MapField;
import sun.misc.Unsafe;

/**
 * 基础CURD操作映射的抽象属性类
 * 
 * @author linbin
 * 
 */
public class MapFieldImpl implements MapField
{
    protected final static Unsafe unsafe = ReflectUtil.getUnsafe();
    protected final long          offset;
    protected final String        dbColName;
    protected final Field         field;
    protected FieldOperator       operator;
    
    public MapFieldImpl(Field field, ColNameStrategy colNameStrategy, FieldOperatorDictionary fieldOperatorDictionary)
    {
        offset = unsafe.objectFieldOffset(field);
        operator = FieldOperatorUtil.getFieldOperator(field, fieldOperatorDictionary);
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
        }
        else
        {
            dbColName = colNameStrategy.toDbName(field.getName());
        }
    }
    
    @Override
    public String getColName()
    {
        return dbColName;
    }
    
    @Override
    public String getFieldName()
    {
        return field.getName();
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
    public FieldOperator fieldOperator()
    {
        return operator;
    }
    
    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException
    {
        operator.setEntityValue(entity, dbColName, resultSet);
    }
    
    @Override
    public Object fieldValue(Object entity)
    {
        return operator.fieldValue(entity);
    }
    
}
