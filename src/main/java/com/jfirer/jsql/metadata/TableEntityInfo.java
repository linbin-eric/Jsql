package com.jfirer.jsql.metadata;

import com.jfirer.baseutil.STR;
import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;
import com.jfirer.jsql.annotation.*;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class TableEntityInfo
{
    public record ColumnInfo(String columnName, String propertyName, Field field, ValueAccessor accessor, String fullname)
    {
    }

    private static final Map<Class<?>, TableEntityInfo> store                 = new ConcurrentHashMap<>();
    private final        String                         className;
    private final        String                         classSimpleName;
    private final        String                         tableName;
    private              Map<String, ColumnInfo>        propertyNameKeyMap;
    private              Map<String, ColumnInfo>        fullnameColumnInfoMap = new HashMap<>();
    private              ColumnInfo                     pkInfo;
    private final        Class<?>                       ckass;
    private              PkGenerator.Generator          pkGenerator;
    private              PkReturnType                   pkReturnType          = PkReturnType.NO_RETURN_PK;
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
        if (!ckass.isAnnotationPresent(TableDef.class))
        {
            throw new IllegalArgumentException(STR.format("类:{}没有使用TableDef注解，不能作为查询条件或者返回结果使用", ckass.getName()));
        }
        tableName = ckass.getAnnotation(TableDef.class).value();
        Map<String, ColumnInfo> propertyNameKeyMap         = new HashMap<>();
        try
        {
            ColumnNameStrategy strategy = ckass.isAnnotationPresent(ColumnName.class) ? //
                    ckass.getAnnotation(ColumnName.class).strategy().getDeclaredConstructor().newInstance()//
                    : ColumnNameStrategy.LOW_CASE;
            for (Field field : getAllFields(ckass))
            {
                if (isNotColumnField(field))
                {
                    continue;
                }
                field.setAccessible(true);
                String     fullName, columnName;
                ColumnName annotation = field.getAnnotation(ColumnName.class);
                if (annotation == null)
                {
                    columnName = strategy.toColumnName(field.getName());
                    fullName   = tableName + '.' + columnName;
                }
                else if (StringUtil.isNotBlank(annotation.fullname()))
                {
                    fullName   = annotation.fullname();
                    columnName = fullName.substring(fullName.lastIndexOf('.') + 1);
                }
                else if (StringUtil.isNotBlank(annotation.value()))
                {
                    columnName = annotation.value();
                    fullName   = tableName + '.' + columnName;
                }
                else
                {
                    throw new IllegalArgumentException("注解ColumnName中value和fullname的值都是空");
                }
                ColumnInfo columnInfo = new ColumnInfo(columnName, field.getName(), field, ValueAccessor.compile(field), fullName);
                propertyNameKeyMap.put(columnInfo.propertyName, columnInfo);
                fullnameColumnInfoMap.put(columnInfo.fullname(), columnInfo);
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
            this.propertyNameKeyMap = Collections.unmodifiableMap(propertyNameKeyMap);
            allColumnInfos          = propertyNameKeyMap.values().toArray(ColumnInfo[]::new);
            allColumnInfosExcludePk = propertyNameKeyMap.values().stream().filter(columnInfo -> columnInfo != pkInfo).toArray(ColumnInfo[]::new);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取该类的所有field对象，如果子类重写了父类的field，则只包含子类的field
     *
     */
    private Field[] getAllFields(Class<?> entityClass)
    {
        // 只需要去重，并且希望父类的field在返回数组中排在后面，所以比较全部返回1
        Set<Field> set = new TreeSet<Field>((o1, o2) -> {
            if (o1.getName().equals(o2.getName()))
            {
                return 0;
            }
            else
            {
                return 1;
            }
        });
        while (entityClass != Object.class && entityClass != null)
        {
            Collections.addAll(set, entityClass.getDeclaredFields());
            entityClass = entityClass.getSuperclass();
        }
        return set.toArray(new Field[0]);
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

    public ColumnInfo findColumnInfoByFullname(String fullname)
    {
        return fullnameColumnInfoMap.get(fullname.toLowerCase());
    }

    public static TableEntityInfo parse(Class<?> entityClass)
    {
        return store.computeIfAbsent(entityClass, TableEntityInfo::new);
    }

    private PkReturnType detectPkValueType(Field pkField)
    {
        return switch (ReflectUtil.getClassId(pkField.getType()))
        {
            case ReflectUtil.CLASS_STRING -> PkReturnType.STRING;
            case ReflectUtil.CLASS_INT -> PkReturnType.INT;
            case ReflectUtil.CLASS_LONG -> PkReturnType.LONG;
            default -> throw new IllegalArgumentException("不支持非String，Integer，Long以外类型的主键,请检查:" + pkField.getDeclaringClass().getName() + "." + pkField.getName());
        };
    }
}
