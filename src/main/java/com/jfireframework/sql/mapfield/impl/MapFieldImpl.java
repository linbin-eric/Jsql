package com.jfireframework.sql.mapfield.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.Column;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;
import com.jfireframework.sql.mapfield.FieldOperator;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.mapfield.MapFieldUtil;
import com.jfireframework.sql.util.JdbcType;
import com.jfireframework.sql.util.JdbcTypeDictionary;
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
    protected final JdbcType      jdbcType;
    protected final String        desc;
    protected FieldOperator       valueFetcher;
    
    public MapFieldImpl(Field field, ColNameStrategy colNameStrategy, JdbcTypeDictionary jdbcTypeDictionary)
    {
        offset = unsafe.objectFieldOffset(field);
        valueFetcher = MapFieldUtil.getFieldOperator(field);
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
            if (JdbcType.ADAPTIVE != column.jdbcType())
            {
                jdbcType = column.jdbcType();
            }
            else
            {
                if (jdbcTypeDictionary.map(field.getType()) == null)
                {
                    if (Enum.class.isAssignableFrom(field.getType()))
                    {
                        jdbcType = jdbcTypeDictionary.map(String.class);
                    }
                    else
                    {
                        throw new NullPointerException(StringUtil.format("字段:{}无法找到对应的sql映射。请进行自定义", field.getDeclaringClass().getName() + "." + field.getName()));
                    }
                }
                else
                {
                    jdbcType = jdbcTypeDictionary.map(field.getType());
                }
            }
            desc = "".equals(column.desc()) ? jdbcType.desc() : column.desc();
        }
        else
        {
            dbColName = colNameStrategy.toDbName(field.getName());
            if (jdbcTypeDictionary.map(field.getType()) == null)
            {
                if (Enum.class.isAssignableFrom(field.getType()))
                {
                    jdbcType = jdbcTypeDictionary.map(String.class);
                }
                else
                {
                    throw new NullPointerException(StringUtil.format("字段:{}无法找到对应的sql映射。请进行自定义", field.getDeclaringClass().getName() + "." + field.getName()));
                }
            }
            else
            {
                jdbcType = jdbcTypeDictionary.map(field.getType());
            }
            desc = jdbcType.desc();
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
    public FieldOperator valueFetcher()
    {
        return valueFetcher;
    }
    
    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException
    {
        valueFetcher.setEntityValue(entity, field, dbColName, offset, resultSet);
    }
    
    @Override
    public Object fieldValue(Object entity)
    {
        return valueFetcher.fieldValue(entity, field, offset);
    }
    
    @Override
    public JdbcType getJdbcType()
    {
        return jdbcType;
    }
    
    @Override
    public String getDesc()
    {
        return desc;
    }
    
}
