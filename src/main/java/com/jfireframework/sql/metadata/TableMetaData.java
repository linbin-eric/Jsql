package com.jfireframework.sql.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.Id;
import com.jfireframework.sql.annotation.SqlIgnore;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.mapfield.impl.MapFieldImpl;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class TableMetaData
{
    private final String          tableName;
    private final MapField[]      fieldInfos;
    private final MapField        idInfo;
    private final Class<?>        ckass;
    private final ColNameStrategy colNameStrategy;
    private final boolean         editable;
    
    public TableMetaData(Class<?> ckass, ColNameStrategy nameStrategy, JdbcTypeDictionary jdbcTypeDictionary)
    {
        this.ckass = ckass;
        this.colNameStrategy = nameStrategy;
        TableEntity entity = ckass.getAnnotation(TableEntity.class);
        editable = entity.editable();
        tableName = entity.name();
        List<MapField> list = new LinkedList<MapField>();
        Field t_idField = null;
        for (Field each : ReflectUtil.getAllFields(ckass))
        {
            if (notTableField(each))
            {
                continue;
            }
            MapField info = new MapFieldImpl(each, nameStrategy, jdbcTypeDictionary);
            list.add(info);
            if (each.isAnnotationPresent(Id.class))
            {
                t_idField = each;
            }
        }
        fieldInfos = list.toArray(new MapField[list.size()]);
        if (t_idField != null)
        {
            if (t_idField.getType().isPrimitive())
            {
                throw new IllegalArgumentException("作为主键的属性不可以使用基本类型，必须使用包装类。请检查" + t_idField.getDeclaringClass().getName() + "." + t_idField.getName());
            }
            idInfo = new MapFieldImpl(t_idField, nameStrategy, jdbcTypeDictionary);
        }
        else
        {
            idInfo = null;
        }
    }
    
    public boolean editable()
    {
        return editable;
    }
    
    private boolean notTableField(Field field)
    {
        if (field.isAnnotationPresent(SqlIgnore.class) //
                || Map.class.isAssignableFrom(field.getType())//
                || List.class.isAssignableFrom(field.getType())//
                || field.getType().isInterface()//
                || Modifier.isStatic(field.getModifiers()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public ColNameStrategy getColNameStrategy()
    {
        return colNameStrategy;
    }
    
    public String getTableName()
    {
        return tableName;
    }
    
    public MapField[] getFieldInfos()
    {
        return fieldInfos;
    }
    
    public MapField getIdInfo()
    {
        return idInfo;
    }
    
    public Class<?> getEntityClass()
    {
        return ckass;
    }
    
}
