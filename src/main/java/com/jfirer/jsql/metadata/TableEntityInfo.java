package com.jfirer.jsql.metadata;

import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jsql.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TableEntityInfo
{
    public record ColumnInfo(String columnName, String propertyName, Field field) {}

    private static final Map<Class<?>, TableEntityInfo> store = new ConcurrentHashMap<Class<?>, TableEntityInfo>();
    private final        String                         className;
    private final        String                         classSimpleName;
    private final        String                         tableName;
    private              Map<String, ColumnInfo>        propertyNameKeyMap;
    private              Map<String, ColumnInfo>        columnNameIgnoreCaseKeyMap;
    private              ColumnInfo                     pkInfo;
    private final        Class<?>                       ckass;

    private TableEntityInfo(Class<?> ckass)
    {
        this.ckass = ckass;
        className = ckass.getName();
        classSimpleName = ckass.getSimpleName();
        tableName = ckass.getAnnotation(TableDef.class).value();
        Map<String, ColumnInfo> propertyNameKeyMap         = new HashMap<String, TableEntityInfo.ColumnInfo>();
        Map<String, ColumnInfo> columnNameIgnoreCaseKeyMap = new HashMap<String, TableEntityInfo.ColumnInfo>();
        try
        {
            ColumnNameStrategy strategy = ckass.isAnnotationPresent(ColumnNameStrategyDef.class) ? //
                    ckass.getAnnotation(ColumnNameStrategyDef.class).value().getDeclaredConstructor().newInstance()//
                    : ColumnNameStrategy.LowerCaseName.instance;
            for (Field field : getAllFields(ckass))
            {
                if (isNotColumnField(field))
                {
                    continue;
                }
                field.setAccessible(true);
                String columnName = field.isAnnotationPresent(ColumnName.class) && StringUtil.isNotBlank(field.getAnnotation(ColumnName.class).value()) ? //
                        field.getAnnotation(ColumnName.class).value()//
                        : strategy.toColumnName(field.getName());
                ColumnInfo columnInfo = new ColumnInfo(columnName, field.getName(), field);
                propertyNameKeyMap.put(columnInfo.propertyName, columnInfo);
                columnNameIgnoreCaseKeyMap.put(columnName.toLowerCase(), columnInfo);
                if (field.isAnnotationPresent(Pk.class))
                {
                    if (pkInfo == null)
                    {
                        pkInfo = new ColumnInfo(columnName, field.getName(), field);
                    }
                    else
                    {
                        throw new IllegalStateException("一个实体类不能注解两个PK注解，请检查" + field.getDeclaringClass().getName());
                    }
                }
            }
            this.propertyNameKeyMap = Collections.unmodifiableMap(propertyNameKeyMap);
            this.columnNameIgnoreCaseKeyMap = Collections.unmodifiableMap(columnNameIgnoreCaseKeyMap);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
        }
    }

    /**
     * 获取该类的所有field对象，如果子类重写了父类的field，则只包含子类的field
     *
     * @param entityClass
     * @return
     */
    private Field[] getAllFields(Class<?> entityClass)
    {
        Set<Field> set = new TreeSet<Field>(new Comparator<Field>()
        {
            // 只需要去重，并且希望父类的field在返回数组中排在后面，所以比较全部返回1
            @Override
            public int compare(Field o1, Field o2)
            {
                if (o1.getName().equals(o2.getName()))
                {
                    return 0;
                }
                else
                {
                    return 1;
                }
            }
        });
        while (entityClass != Object.class && entityClass != null)
        {
            for (Field each : entityClass.getDeclaredFields())
            {
                set.add(each);
            }
            entityClass = entityClass.getSuperclass();
        }
        return set.toArray(new Field[set.size()]);
    }

    private boolean isNotColumnField(Field field)
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
        return Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type);
    }

    public String getClassSimpleName()
    {
        return classSimpleName;
    }

    public String getTableName()
    {
        return tableName;
    }

    public Map<String, ColumnInfo> getPropertyNameKeyMap()
    {
        return propertyNameKeyMap;
    }

    public ColumnInfo getPkInfo()
    {
        return pkInfo;
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

    public ColumnInfo getColumnInfoByColumnNameIgnoreCase(String columnName)
    {
        return columnNameIgnoreCaseKeyMap.get(columnName.toLowerCase());
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
