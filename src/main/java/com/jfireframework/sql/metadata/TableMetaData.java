package com.jfireframework.sql.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.Column;
import com.jfireframework.sql.annotation.Id;
import com.jfireframework.sql.annotation.SqlIgnore;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.mapfield.impl.MapFieldImpl;
import com.jfireframework.sql.util.JdbcType;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class TableMetaData
{
    private final String                   tableName;
    private final MapField[]               fieldInfos;
    private final MapField                 idInfo;
    private final Map<MapField, FieldDesc> descMap = new HashMap<MapField, FieldDesc>();
    private final Class<?>                 ckass;
    private final ColNameStrategy          colNameStrategy;
    private final boolean                  editable;
    
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
            MapField mapField = new MapFieldImpl(each, nameStrategy);
            list.add(mapField);
            descMap.put(mapField, buildFieldDesc(each, jdbcTypeDictionary));
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
            idInfo = new MapFieldImpl(t_idField, nameStrategy);
        }
        else
        {
            idInfo = null;
        }
    }
    
    FieldDesc buildFieldDesc(Field field, JdbcTypeDictionary jdbcTypeDictionary)
    {
        JdbcType jdbcType;
        String desc;
        if (field.isAnnotationPresent(Column.class))
        {
            Column column = field.getAnnotation(Column.class);
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
        FieldDesc fieldDesc = new FieldDesc();
        fieldDesc.jdbcType = jdbcType;
        fieldDesc.desc = desc;
        return fieldDesc;
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
    
    public FieldDesc getFieldDesc(MapField mapField)
    {
        return descMap.get(mapField);
    }
    
    public static class FieldDesc
    {
        private JdbcType jdbcType;
        private String   desc;
        
        public JdbcType getJdbcType()
        {
            return jdbcType;
        }
        
        public void setJdbcType(JdbcType jdbcType)
        {
            this.jdbcType = jdbcType;
        }
        
        public String getDesc()
        {
            return desc;
        }
        
        public void setDesc(String desc)
        {
            this.desc = desc;
        }
        
    }
}
