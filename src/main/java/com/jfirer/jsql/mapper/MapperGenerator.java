package com.jfirer.jsql.mapper;

import com.jfirer.jfireel.expression.token.Operator;
import com.jfirer.jsql.analyse.template.Template;
import com.jfirer.jsql.analyse.token.SqlLexer;
import com.jfirer.jsql.annotation.Sql;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.transfer.resultset.ResultMap;
import com.jfirer.jsql.transfer.resultset.ResultSetTransfer;
import com.jfirer.jsql.transfer.resultset.impl.*;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.smc.SmcHelper;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.FieldModel;
import com.jfirer.baseutil.smc.model.MethodModel;

import javax.swing.*;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MapperGenerator
{
    private static final AtomicInteger count = new AtomicInteger(0);

    public static Class<?> generate(Class<?> ckass, Map<String, TableEntityInfo> tableEntityInfos, CompileHelper compiler)
    {
        Method[] methods = ckass.getMethods();
        for (Method method : methods)
        {
            if (method.isAnnotationPresent(Sql.class) == false)
            {
//                throw new IllegalArgumentException("类:" + method.getDeclaringClass().getName() + "有方法没有打@Sql注解");
            }
        }
        ClassModel classModel = new ClassModel(ckass.getSimpleName() + "$Mapper$" + count.getAndIncrement(), AbstractMapper.class, ckass);
        classModel.addImport(AbstractMapper.class);
        classModel.addImport(Template.class);
        classModel.addImport(Map.class);
        classModel.addImport(HashMap.class);
        classModel.addImport(String.class);
        classModel.addImport(BeanTransfer.class);
        classModel.addImport(SqlSession.class);
        classModel.addImport(List.class);
        AtomicInteger fieldNameCount = new AtomicInteger(0);
        for (Method method : methods)
        {
            generateByAnnotation(tableEntityInfos, classModel, fieldNameCount, method);
        }
        Thread.currentThread().getContextClassLoader();
        try
        {
            return compiler.compile(classModel);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    /**
     * 通过类似JPA的形式生成具体实现
     *
     * @param tableEntityInfos
     * @param classModel
     * @param fieldNameCount
     * @param method
     */
    private static void generateByJpaMode(Class ckass, Map<String, TableEntityInfo> tableEntityInfos, ClassModel classModel, AtomicInteger fieldNameCount, Method method)
    {
        Class entityClass = null;
        for (Type each : ckass.getGenericInterfaces())
        {
            if (each instanceof ParameterizedType && ((ParameterizedType) each).getRawType() == Repository.class)
            {
                entityClass = (Class) ((ParameterizedType) each).getActualTypeArguments()[0];
                break;
            }
        }
        if (entityClass == null)
        {
            throw new NullPointerException("接口：" + ckass.getName() + "没有继承Repository，无法实现JPA模式方法");
        }
        TriTree operatrors = new TriTree();
        for (Operator each : Operator.values())
        {
            operatrors.set(each.name());
        }
        TriTree         propertyNames   = new TriTree();
        TableEntityInfo tableEntityInfo = tableEntityInfos.get(entityClass.getSimpleName());
        for (String each : tableEntityInfo.getPropertyNameKeyMap().keySet())
        {
            String transferPropertyName = each.substring(0, 1).toUpperCase() + each.substring(1);
            propertyNames.set(transferPropertyName);
        }
        String methodName = method.getName();
        if (methodName.startsWith("find"))
        {
            StringBuilder builder = new StringBuilder();
            builder.append("select * from ").append(tableEntityInfo.getTableName()).append(" ");
            int length = methodName.length();
            int index  = 4;
            while (index < length)
            {
                String content      = methodName.substring(index);
                String operatorName = operatrors.find(content);
                if (operatorName == null)
                {
                    throw new IllegalArgumentException("方法：" + method.toGenericString() + "不匹配操作符");
                }
                Operator operator = Operator.valueOf(operatorName);
                switch (operator)
                {
                    case By:
                    {
                        index += 2;
                        content = methodName.substring(index);
                        String propername = propertyNames.find(content);
                        if (propername == null)
                        {
                            throw new IllegalArgumentException("方法：" + method.toGenericString() + "中：" + propername + "不匹配属性名");
                        }
                        index += propername.length();
                        propername = propername.substring(0, 1).toLowerCase() + propername.substring(1);
                        TableEntityInfo.ColumnInfo columnInfo = tableEntityInfo.getPropertyNameKeyMap().get(propername);
                        builder.append(columnInfo.getColumnName()).append(" ");
                        operatorName = operatrors.find(methodName.substring(index));
                        if (operatorName != null && (Operator.And.name().equals(operatorName) || Operator.Or.equals(operatorName)))
                        {
                            builder.append(" = ? ");
                        }
                        break;
                    }
                    case And:
                    case Or:
                    {
                        builder.append(" ").append(operatorName.toLowerCase()).append(' ');
                        index += operatorName.length();
                        break;
                    }
                    case LessThan:
                    {
                        builder.append(" < ? ");
                        break;
                    }
                    case LessThanEqual:
                }
            }
        }
    }

    enum Operator
    {
        And("and"), Or("or"),//
        By("by"),//
        Between("between"), LessThan("<"), LessThanEqual("<="), GreaterThan(">"), GreaterThanEqual(">="), After(">"), Before("<"),//
        Like("like"), NotLike("not like"), StartingWith(""), EndingWith, Containing, OrderBy, In, NotIn,//
        True, False, IsNull, IsNotNull,
        ;
        private String literal;

        Operator(String literal)
        {
            this.literal = literal;
        }
    }

    static class TriTree extends HashMap<Character, TriTree>
    {
        String destination;

        private void put(String content, int index)
        {
            if (content.length() == index + 1)
            {
                destination = content;
            }
            else
            {
                index++;
                char    c         = content.charAt(index);
                TriTree symolTree = get(c);
                if (symolTree == null)
                {
                    symolTree = new TriTree();
                    put(c, symolTree);
                }
                symolTree.put(content, index);
            }
        }

        private String get(String content, int index)
        {
            if (index == content.length() - 1)
            {
                return content.equals(destination) ? destination : null;
            }
            index++;
            TriTree symolTree = get(content.charAt(index));
            return symolTree == null ? destination : symolTree.get(content, index);
        }

        public String find(String content)
        {
            return get(content, -1);
        }

        public void set(String content)
        {
            put(content, -1);
        }
    }

    /**
     * 通过方法上的Sql注解为其生成具体的方法体。
     *
     * @param tableEntityInfos
     * @param classModel
     * @param fieldNameCount
     * @param method
     */
    private static void generateByAnnotation(Map<String, TableEntityInfo> tableEntityInfos, ClassModel classModel, AtomicInteger fieldNameCount, Method method)
    {
        StringBuilder cache = new StringBuilder();
        cache.append("if(session==null){throw new NullPointerException(\"当前没有session\");}");
        cache.append("Map<String,Object> variables = cachedVariables.get();\r\n");
        cache.append("List<Object> params = cachedParams.get();\r\n");
        MethodModel methodModel = new MethodModel(method, classModel);
        Sql         annotation  = method.getAnnotation(Sql.class);
        String      formatSql   = generateSqlAndTemplateField(tableEntityInfos, classModel, fieldNameCount, method, cache, annotation);
        if (formatSql.startsWith("SELECT"))
        {
            String transferFieldName = "transfer_" + (fieldNameCount.getAndIncrement());
            if (List.class.isAssignableFrom(method.getReturnType()))
            {
                Class<?> componentClass = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                addResultSetTransferField(classModel, method, transferFieldName, componentClass);
                cache.append("List result = session.queryList(").append(transferFieldName).append(",sql,params);\r\n");
            }
            else
            {
                addResultSetTransferField(classModel, method, transferFieldName, method.getReturnType());
                String returnTypeName = method.getReturnType().isPrimitive() ? ReflectUtil.wrapPrimitive(method.getReturnType()).getName() : SmcHelper.getReferenceName(method.getReturnType(), classModel);
                cache.append(returnTypeName).append(" result = session.query(").append(transferFieldName).append(",sql,params);\r\n");
            }
        }
        else
        {
            cache.append("int result = session.update(sql,params);\r\n");
        }
        cache.append("params.clear();\r\n");
        cache.append("variables.clear();\r\n");
        cache.append("return result;\r\n");
        methodModel.setBody(cache.toString());
        classModel.putMethodModel(methodModel);
    }

    /**
     * 生成ResultSetTransferField字段，并且添加到ClassModel中。
     *
     * @param classModel
     * @param method
     * @param transferFieldName
     * @param itemType          返回参数的类型。如果方法返回是List，则取其泛型参数的类型
     */
    private static void addResultSetTransferField(ClassModel classModel, Method method, String transferFieldName, Class<?> itemType)
    {
        Class<? extends ResultSetTransfer> ckass = null;
        if (method.isAnnotationPresent(ResultMap.class))
        {
            ckass = method.getAnnotation(ResultMap.class).value();
        }
        else if (itemType == String.class)
        {
            ckass = StringTransfer.class;
        }
        else if (Enum.class.isAssignableFrom(itemType))
        {
            ckass = EnumNameTransfer.class;
        }
        else if (itemType == Date.class)
        {
            ckass = SqlDateTransfer.class;
        }
        else if (itemType == java.util.Date.class)
        {
            ckass = UtilDateTransfer.class;
        }
        else if (itemType == Timestamp.class)
        {
            ckass = TimeStampTransfer.class;
        }
        else if (itemType == Time.class)
        {
            ckass = TimeTransfer.class;
        }
        else if (itemType.isPrimitive())
        {
            itemType = ReflectUtil.wrapPrimitive(itemType);
            if (itemType == Integer.class)
            {
                ckass = IntegerTransfer.class;
            }
            else if (itemType == Long.class)
            {
                ckass = LongTransfer.class;
            }
            else if (itemType == Short.class)
            {
                ckass = ShortTransfer.class;
            }
            else if (itemType == Float.class)
            {
                ckass = FloatTransfer.class;
            }
            else if (itemType == Double.class)
            {
                ckass = DoubleTransfer.class;
            }
            else if (itemType == Boolean.class)
            {
                ckass = BooleanTransfer.class;
            }
            else
            {
                throw new UnsupportedOperationException("不支持的单类型转换:" + itemType.getName());
            }
        }
        else
        {
            ckass = BeanTransfer.class;
        }
        classModel.addImport(ckass);
        FieldModel fieldModel = new FieldModel(transferFieldName, ResultSetTransfer.class, "new " + SmcHelper.getReferenceName(ckass, classModel) + "().initialize(" + SmcHelper.getReferenceName(itemType, classModel) + ".class)", classModel);
        classModel.addField(fieldModel);
    }

    /**
     * 生成并添加模板字段，并且生成解析格式化Sql的代码。最终返回格式化的sql
     *
     * @param tableEntityInfos
     * @param classModel
     * @param fieldNameCount
     * @param method
     * @param cache
     * @param annotation
     * @return
     */
    private static String generateSqlAndTemplateField(Map<String, TableEntityInfo> tableEntityInfos, ClassModel classModel, AtomicInteger fieldNameCount, Method method, StringBuilder cache, Sql annotation)
    {
        String     formatSql         = SqlLexer.parse(annotation.sql()).transfer(tableEntityInfos).format();
        String     templateFieldName = "template_" + (fieldNameCount.getAndIncrement());
        FieldModel fieldModel        = new FieldModel(templateFieldName, Template.class, "Template.parse(\"" + formatSql + "\")", classModel);
        classModel.addField(fieldModel);
        Class<?>[] parameterTypes = method.getParameterTypes();
        String     paramNames     = annotation.paramNames();
        if (parameterTypes.length != 0)
        {
            String[] names = paramNames.split(",");
            int      index = 0;
            for (String each : names)
            {
                cache.append("variables.put(\"").append(each).append("\",$").append(index).append(");\r\n");
                index++;
            }
        }
        cache.append("String sql =").append(templateFieldName).append(".render(variables,params);\r\n");
        if (parameterTypes.length != 0 && parameterTypes[parameterTypes.length - 1] == Page.class)
        {
            cache.append("params.add($").append(parameterTypes.length - 1).append(");\r\n");
        }
        return formatSql;
    }
}
