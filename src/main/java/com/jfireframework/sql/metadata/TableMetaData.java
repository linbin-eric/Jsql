package com.jfireframework.sql.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Blob;
import java.sql.Clob;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.annotation.Id;
import com.jfireframework.sql.annotation.SqlIgnore;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.dbstructure.column.UserDefinedColumnType;
import com.jfireframework.sql.dbstructure.column.ColumnType;
import com.jfireframework.sql.dbstructure.column.ColumnTypeDictionary;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.mapfield.impl.MapFieldImpl;

public class TableMetaData
{
    private final String                    tableName;
    private final MapField[]                fieldInfos;
    private final MapField                  idInfo;
    private final Map<MapField, ColumnType> columnTypeMap = new HashMap<MapField, ColumnType>();
    private final Class<?>                  ckass;
    private final ColNameStrategy           colNameStrategy;
    private final boolean                   editable;
    
    public TableMetaData(Class<?> ckass, ColNameStrategy nameStrategy, SessionfactoryConfig config)
    {
        this.ckass = ckass;
        this.colNameStrategy = nameStrategy;
        TableEntity entity = ckass.getAnnotation(TableEntity.class);
        editable = entity.editable();
        tableName = entity.name().toUpperCase();
        List<MapField> list = new LinkedList<MapField>();
        Field t_idField = null;
        for (Field each : ReflectUtil.getAllFields(ckass))
        {
            if (notTableField(each))
            {
                continue;
            }
            MapField mapField = new MapFieldImpl(each, nameStrategy, config.getFieldOperatorDictionary());
            list.add(mapField);
            columnTypeMap.put(mapField, buildColumnType(each, config.getJdbcTypeDictionary()));
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
            idInfo = new MapFieldImpl(t_idField, nameStrategy, config.getFieldOperatorDictionary());
            columnTypeMap.put(idInfo, buildColumnType(t_idField, config.getJdbcTypeDictionary()));
        }
        else
        {
            idInfo = null;
        }
    }
    
    ColumnType buildColumnType(Field field, ColumnTypeDictionary jdbcTypeDictionary)
    {
        ColumnType columnType;
        if (field.isAnnotationPresent(UserDefinedColumnType.class))
        {
            final UserDefinedColumnType columnDesc = field.getAnnotation(UserDefinedColumnType.class);
            columnType = new ColumnType() {
                
                @Override
                public String type()
                {
                    return columnDesc.type().toUpperCase();
                }
                
                @Override
                public String desc()
                {
                    return columnDesc.desc().toUpperCase();
                }
            };
        }
        else
        {
            if (jdbcTypeDictionary.map(field.getType()) == null)
            {
                if (Enum.class.isAssignableFrom(field.getType()))
                {
                    columnType = jdbcTypeDictionary.map(String.class);
                }
                else
                {
                    throw new NullPointerException(StringUtil.format("字段:{}无法找到对应的sql映射。请进行自定义", field.getDeclaringClass().getName() + "." + field.getName()));
                }
            }
            else
            {
                columnType = jdbcTypeDictionary.map(field.getType());
            }
        }
        return columnType;
    }
    
    public boolean editable()
    {
        return editable;
    }
    
    private boolean notTableField(Field field)
    {
        if (field.getType() == Clob.class || field.getType() == Blob.class)
        {
            return false;
        }
        if (field.isAnnotationPresent(SqlIgnore.class) //
                || Map.class.isAssignableFrom(field.getType())//
                || List.class.isAssignableFrom(field.getType())//
                || (field.getType().isInterface())//
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
    
    public ColumnType columnType(MapField mapField)
    {
        return columnTypeMap.get(mapField);
    }
}
