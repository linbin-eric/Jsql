package cc.jfire.jsql.metadata;

import cc.jfire.baseutil.StringUtil;
import cc.jfire.baseutil.reflect.ReflectUtil;
import cc.jfire.baseutil.reflect.valueaccessor.ValueAccessor;
import cc.jfire.jsql.annotation.*;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class TableEntityInfo
{
    public record ColumnInfo(String columnName, String propertyName, String tableName, Field field, ValueAccessor accessor)
    {
    }

    private static final Map<Class<?>, TableEntityInfo> store              = new ConcurrentHashMap<>();
    private final        String                         classSimpleName;
    private final        String                         tableName;
    private              Map<String, ColumnInfo>        propertyNameKeyMap = new HashMap<>();
    private              Map<String, ColumnInfo>        columnNameKeyMap   = new HashMap<>();
    private              ColumnInfo                     pkInfo;
    private final        Class<?>              ckass;
    private              PkGenerator.Generator pkGenerator;
    private              PkReturnType          pkReturnType       = PkReturnType.NO_RETURN_PK;
    private final        ColumnInfo[]                   allColumnInfos;
    private final        ColumnInfo[]                   allColumnInfosExcludePk;

    public enum PkReturnType
    {
        STRING, INT, LONG, NO_RETURN_PK
    }

    private TableEntityInfo(Class<?> ckass)
    {
        this.ckass      = ckass;
        classSimpleName = ckass.getSimpleName();
        tableName       = ckass.isAnnotationPresent(TableDef.class) ? ckass.getAnnotation(TableDef.class).value() : classSimpleName;
        try
        {
            ColumnNameStrategy strategy = ckass.isAnnotationPresent(ColumnName.class) ? //
                    ckass.getAnnotation(ColumnName.class).strategy().getDeclaredConstructor().newInstance()//
                    : ColumnNameStrategy.LOW_CASE;
            for (Field field : getAllSuitableFields(ckass))
            {
                field.setAccessible(true);
                String columnName;
                if (field.isAnnotationPresent(ColumnName.class))
                {
                    ColumnName annotation = field.getAnnotation(ColumnName.class);
                    if (StringUtil.isNotBlank(annotation.value()))
                    {
                        columnName = annotation.value();
                    }
                    else
                    {
                        ColumnNameStrategy columnNameStrategy = ColumnNameStrategy.find(annotation.strategy());
                        columnName = columnNameStrategy.toColumnName(field.getName());
                    }
                }
                else
                {
                    columnName = strategy.toColumnName(field.getName());
                }
                String     tableName  = field.isAnnotationPresent(TableName.class) ? field.getAnnotation(TableName.class).value() : null;
                ColumnInfo columnInfo = new ColumnInfo(columnName.toLowerCase(), field.getName(), tableName, field, ValueAccessor.compile(field));
                propertyNameKeyMap.put(columnInfo.propertyName, columnInfo);
                columnNameKeyMap.put(columnInfo.columnName(), columnInfo);
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
                        pkReturnType = detectPkValueType(field, ckass);
                    }
                    else
                    {
                    }
                }
            }
            allColumnInfos          = propertyNameKeyMap.values().toArray(ColumnInfo[]::new);
            allColumnInfosExcludePk = propertyNameKeyMap.values().stream().filter(columnInfo -> columnInfo != pkInfo).toArray(ColumnInfo[]::new);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取该类的所有的合适sql的field对象，如果子类重写了父类的field，则只包含子类的field
     */
    private Field[] getAllSuitableFields(Class<?> entityClass)
    {
        Map<String, Field> map = new HashMap<>();
        while (entityClass != Object.class && entityClass != null)
        {
            for (Field each : entityClass.getDeclaredFields())
            {
                if (!notSuitableField(each))
                {
                    map.putIfAbsent(each.getName(), each);
                }
            }
            entityClass = entityClass.getSuperclass();
        }
        return map.values().toArray(new Field[0]);
    }

    private boolean notSuitableField(Field field)
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

    public static TableEntityInfo parse(Class<?> entityClass)
    {
        return store.computeIfAbsent(entityClass, TableEntityInfo::new);
    }

    private PkReturnType detectPkValueType(Field pkField, Class<?> origin)
    {
        Class<?> pkType = pkField.getType();
        // 如果pkField的类型是泛型参数，需要通过origin来解析实际类型
        Type genericType = pkField.getGenericType();
        if (genericType instanceof TypeVariable<?> typeVariable)
        {
            pkType = resolveTypeVariable(typeVariable, origin);
        }
        return switch (ReflectUtil.getClassId(pkType))
        {
            case ReflectUtil.CLASS_STRING -> PkReturnType.STRING;
            case ReflectUtil.CLASS_INT -> PkReturnType.INT;
            case ReflectUtil.CLASS_LONG -> PkReturnType.LONG;
            default -> throw new IllegalArgumentException("不支持非String，Integer，Long以外类型的主键,请检查:" + pkField.getDeclaringClass().getName() + "." + pkField.getName());
        };
    }

    /**
     * 解析泛型类型变量的实际类型
     * 通过origin类的继承链向上查找，找到定义该泛型参数的类，并获取实际类型
     */
    private Class<?> resolveTypeVariable(TypeVariable<?> typeVariable, Class<?> origin)
    {
        // 获取泛型参数定义所在的类
        Class<?> declaringClass = (Class<?>) typeVariable.getGenericDeclaration();
        String typeParamName = typeVariable.getName();
        // 从origin开始向上查找继承链
        Class<?> currentClass = origin;
        while (currentClass != null && currentClass != Object.class)
        {
            Type genericSuperclass = currentClass.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType parameterizedType)
            {
                // 检查是否是我们要找的那个泛型父类
                if (parameterizedType.getRawType() == declaringClass)
                {
                    // 找到泛型参数在父类定义中的位置
                    TypeVariable<?>[] typeParameters = declaringClass.getTypeParameters();
                    for (int i = 0; i < typeParameters.length; i++)
                    {
                        if (typeParameters[i].getName().equals(typeParamName))
                        {
                            Type actualType = parameterizedType.getActualTypeArguments()[i];
                            if (actualType instanceof Class<?>)
                            {
                                return (Class<?>) actualType;
                            }
                            else if (actualType instanceof TypeVariable<?> nestedTypeVar)
                            {
                                // 如果实际类型还是泛型变量，继续递归解析
                                return resolveTypeVariable(nestedTypeVar, origin);
                            }
                        }
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        throw new IllegalArgumentException("无法解析泛型类型: " + typeVariable.getName() + "，请检查类: " + origin.getName());
    }

    @Data
    public static  class BaseEntity<T>{
        @Pk
        @AutoIncrement
        private T id;

    }

    public static class  User extends  BaseEntity<Long>{

    }

    public static void main(String[] args)
    {
        TableEntityInfo tableEntityInfo = new TableEntityInfo(User.class);
        System.out.println(tableEntityInfo.getPkInfo().columnName);
    }
}
