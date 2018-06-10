package com.jfireframework.sql.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.ColumnDef;
import com.jfireframework.sql.annotation.ColumnNameStrategyDef;
import com.jfireframework.sql.annotation.Pk;
import com.jfireframework.sql.annotation.SqlIgnore;
import com.jfireframework.sql.annotation.TableDef;
import com.jfireframework.sql.metadata.ColumnNameStrategy;
import com.jfireframework.sql.metadata.DefaultLowerCaseNameStrategy;

public class TableEntityInfo
{
    private static final Map<Class<?>, TableEntityInfo> store = new ConcurrentHashMap<Class<?>, TableEntityInfo>();
    
    private String                                      className;
    private String                                      classSimpleName;
    private String                                      tableName;
    private Map<String, String>                         propertyNameToColumnNameMap;
    private Map<String, Field>                          columnNameToFieldMap;
    private Map<String, Field>                          columnNameIgnoreCaseToFieldMap;
    private Field                                       pkField;
    private Class<?>                                    ckass;
    
    private TableEntityInfo(Class<?> ckass)
    {
        this.ckass = ckass;
        className = ckass.getName();
        classSimpleName = ckass.getName();
        tableName = ckass.getAnnotation(TableDef.class).name();
        Map<String, String> propertyNameToColumnNameMap = new HashMap<String, String>();
        Map<String, Field> columnNameToFieldMap = new HashMap<String, Field>();
        try
        {
            ColumnNameStrategy strategy = ckass.isAnnotationPresent(ColumnNameStrategyDef.class) ? //
                    ckass.getAnnotation(ColumnNameStrategyDef.class).value().newInstance()//
                    : DefaultLowerCaseNameStrategy.instance;
            for (Field field : ReflectUtil.getAllFields(ckass))
            {
                if (isNotColumnField(field))
                {
                    continue;
                }
                if (field.isAnnotationPresent(ColumnDef.class) && StringUtil.isNotBlank(field.getAnnotation(ColumnDef.class).columnName()))
                {
                    String columnName = field.getAnnotation(ColumnDef.class).columnName();
                    propertyNameToColumnNameMap.put(field.getName(), columnName);
                    columnNameToFieldMap.put(columnName, field);
                    field.setAccessible(true);
                }
                else
                {
                    String columnName = strategy.toColumnName(field.getName());
                    propertyNameToColumnNameMap.put(field.getName(), columnName);
                    columnNameToFieldMap.put(columnName, field);
                    field.setAccessible(true);
                }
                if (field.isAnnotationPresent(Pk.class))
                {
                    if (pkField == null)
                    {
                        pkField = field;
                        pkField.setAccessible(true);
                    }
                    else
                    {
                        throw new IllegalStateException("一个实体类不能注解两个PK注解，请检查" + field.getDeclaringClass().getName());
                    }
                }
            }
            this.propertyNameToColumnNameMap = Collections.unmodifiableMap(propertyNameToColumnNameMap);
            this.columnNameToFieldMap = Collections.unmodifiableMap(columnNameToFieldMap);
            columnNameIgnoreCaseToFieldMap = new HashMap<String, Field>();
            for (Entry<String, Field> entry : columnNameToFieldMap.entrySet())
            {
                columnNameIgnoreCaseToFieldMap.put(entry.getKey().toLowerCase(), entry.getValue());
            }
            this.columnNameIgnoreCaseToFieldMap = Collections.unmodifiableMap(columnNameIgnoreCaseToFieldMap);
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
    }
    
    protected boolean isNotColumnField(Field field)
    {
        if (field.isAnnotationPresent(SqlIgnore.class))
        {
            return true;
        }
        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers))
        {
            return true;
        }
        Class<?> type = field.getType();
        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type))
        {
            return true;
        }
        return false;
    }
    
    public String getClassSimpleName()
    {
        return classSimpleName;
    }
    
    public String getTableName()
    {
        return tableName;
    }
    
    public Map<String, String> getPropertyNameToColumnNameMap()
    {
        return propertyNameToColumnNameMap;
    }
    
    public Map<String, Field> getColumnNameToFieldMap()
    {
        return columnNameToFieldMap;
    }
    
    public Field getPkField()
    {
        return pkField;
    }
    
    @Override
    public String toString()
    {
        return "TableTransfer [className=" + className + ", tableName=" + tableName + "]";
    }
    
    public Class<?> getEntityClass()
    {
        return ckass;
    }
    
    public Field getFieldByColumnNameIgnoreCase(String columnName)
    {
        return columnNameIgnoreCaseToFieldMap.get(columnName.toLowerCase());
    }
    
    public static TableEntityInfo parse(Class<?> entityClass)
    {
        TableEntityInfo tableEntityInfo = store.get(entityClass);
        if (tableEntityInfo == null)
        {
            tableEntityInfo = new TableEntityInfo(entityClass);
            store.put(entityClass, tableEntityInfo);
        }
        return tableEntityInfo;
    }
    
}
