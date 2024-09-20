package com.jfirer.jsql.metadata;

import com.jfirer.baseutil.STR;
import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;
import com.jfirer.jsql.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TableEntityInfo
{
    public record ColumnInfo(String columnName, String propertyName, Field field, ValueAccessor accessor)
    {
    }

    private static final Map<Class<?>, TableEntityInfo> store        = new ConcurrentHashMap<Class<?>, TableEntityInfo>();
    private final        String                         className;
    private final        String                         classSimpleName;
    private final        String                         tableName;
    private              Map<String, ColumnInfo>        propertyNameKeyMap;
    private              Map<String, ColumnInfo>        columnNameIgnoreCaseKeyMap;
    private              ColumnInfo                     pkInfo;
    private final        Class<?>                       ckass;
    private              PkGenerator.Generator          pkGenerator;
    private              PkReturnType                   pkReturnType = PkReturnType.NO_RETURN_PK;
    private final        ColumnInfo[]                   allColumnInfos;
    private final        ColumnInfo[]                   allColumnInfosExcludePk;

    public enum PkReturnType
    {
        STRING, INT, LONG, NO_RETURN_PK
    }

    private TableEntityInfo(Class<?> ckass)
    {
        this.ckass      = ckass;
        className       = ckass.getName();
        classSimpleName = ckass.getSimpleName();
        if (ckass.isAnnotationPresent(TableDef.class) == false)
        {
            throw new IllegalArgumentException(STR.format("类:{}没有使用TableDef注解，不能作为查询条件或者返回结果使用", ckass.getName()));
        }
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
                ColumnInfo columnInfo = new ColumnInfo(columnName, field.getName(), field, ValueAccessor.compile(field));
                propertyNameKeyMap.put(columnInfo.propertyName, columnInfo);
                columnNameIgnoreCaseKeyMap.put(columnName.toLowerCase(), columnInfo);
                if (field.isAnnotationPresent(Pk.class))
                {
                    if (pkInfo == null)
                    {
                        pkInfo = columnInfo;
                    }
                    else
                    {
                        throw new IllegalStateException("一个实体类不能注解两个PK注解，请检查" + field.getDeclaringClass().getName());
                    }
                    if (field.isAnnotationPresent(PkGenerator.class))
                    {
                        pkGenerator = field.getAnnotation(PkGenerator.class).value().getDeclaredConstructor().newInstance();
                    }
                    else if (field.isAnnotationPresent(AutoIncrement.class) || field.isAnnotationPresent(Sequence.class))
                    {
                        pkReturnType = detectPkValueType(field);
                    }
                    else
                    {
                    }
                }
            }
            this.propertyNameKeyMap         = Collections.unmodifiableMap(propertyNameKeyMap);
            this.columnNameIgnoreCaseKeyMap = Collections.unmodifiableMap(columnNameIgnoreCaseKeyMap);
            allColumnInfos                  = propertyNameKeyMap.values().toArray(ColumnInfo[]::new);
            allColumnInfosExcludePk         = propertyNameKeyMap.values().stream().filter(columnInfo -> columnInfo != pkInfo).toArray(ColumnInfo[]::new);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
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
            Collections.addAll(set, entityClass.getDeclaredFields());
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
        return store.computeIfAbsent(entityClass, ckass -> new TableEntityInfo(ckass));
    }

    public PkGenerator.Generator getPkGenerator()
    {
        return pkGenerator;
    }

    private PkReturnType detectPkValueType(Field pkField)
    {
        if (pkField.getType() == String.class)
        {
            return PkReturnType.STRING;
        }
        else if (pkField.getType() == Integer.class)
        {
            return PkReturnType.INT;
        }
        else if (pkField.getGenericType() == Long.class)
        {
            return PkReturnType.LONG;
        }
        else
        {
            throw new IllegalArgumentException("不支持非String，Integer，Long以外类型的主键,请检查:" + pkField.getDeclaringClass().getName() + "." + pkField.getName());
        }
    }

    public PkReturnType getPkReturnType()
    {
        return pkReturnType;
    }

    public ColumnInfo[] getAllColumnInfos()
    {
        return allColumnInfos;
    }

    public ColumnInfo[] getAllColumnInfosExcludePk()
    {
        return allColumnInfosExcludePk;
    }
}
