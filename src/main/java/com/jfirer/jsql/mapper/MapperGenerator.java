package com.jfirer.jsql.mapper;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.smc.SmcHelper;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.FieldModel;
import com.jfirer.baseutil.smc.model.MethodModel;
import com.jfirer.jsql.analyse.template.Template;
import com.jfirer.jsql.analyse.token.SqlLexer;
import com.jfirer.jsql.annotation.Sql;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.transfer.resultset.ResultMap;
import com.jfirer.jsql.transfer.resultset.ResultSetTransfer;
import com.jfirer.jsql.transfer.resultset.impl.*;

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
        ClassModel      classModel      = buildClassModelAndImportNecessaryClass(ckass);
        AtomicInteger   fieldNameCount  = new AtomicInteger(0);
        TableEntityInfo tableEntityInfo = null;
        TriTree         operators       = null;
        TriTree         propertyNames   = null;
        for (Method method : ckass.getDeclaredMethods())
        {
            StringBuilder methodBody = new StringBuilder();
            methodBody.append("if(session==null){throw new NullPointerException(\"当前没有session\");}");
            methodBody.append("Map<String,Object> variables = cachedVariables.get();\r\n");
            methodBody.append("List<Object> params = cachedParams.get();\r\n");
            MethodModel methodModel = new MethodModel(method, classModel);
            String      formatSql;
            if (method.isAnnotationPresent(Sql.class))
            {
                formatSql = generateSqlAndTemplateField(tableEntityInfos, classModel, fieldNameCount, method, methodBody);
            }
            else
            {
                tableEntityInfo = tableEntityInfo == null ? getTableEntityInfoFromInterface(ckass, tableEntityInfos) : tableEntityInfo;
                operators = operators == null ? getOperators() : operators;
                propertyNames = propertyNames == null ? getPropertyNames(tableEntityInfo) : propertyNames;
                formatSql = generateSqlAndTemplateField(tableEntityInfo, operators, propertyNames, classModel, fieldNameCount, method, methodBody);
            }
            if (formatSql.startsWith("SELECT") || formatSql.startsWith("select"))
            {
                String transferFieldName = "transfer_" + (fieldNameCount.getAndIncrement());
                if (List.class.isAssignableFrom(method.getReturnType()))
                {
                    Class<?> componentClass = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                    addResultSetTransferField(classModel, method, transferFieldName, componentClass);
                    methodBody.append("List result = session.queryList(").append(transferFieldName).append(",sql,params);\r\n");
                }
                else
                {
                    addResultSetTransferField(classModel, method, transferFieldName, method.getReturnType());
                    String returnTypeName = method.getReturnType().isPrimitive() ? ReflectUtil.wrapPrimitive(method.getReturnType()).getName() : SmcHelper.getReferenceName(method.getReturnType(), classModel);
                    methodBody.append(returnTypeName).append(" result = session.query(").append(transferFieldName).append(",sql,params);\r\n");
                }
            }
            else
            {
                methodBody.append("int result = session.update(sql,params);\r\n");
            }
            methodBody.append("params.clear();\r\n");
            methodBody.append("variables.clear();\r\n");
            if (method.getReturnType() == void.class || method.getReturnType() == Void.class)
            {
                ;
            }
            else
            {
                methodBody.append("return result;\r\n");
            }
            methodModel.setBody(methodBody.toString());
            classModel.putMethodModel(methodModel);
        }
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

    private static ClassModel buildClassModelAndImportNecessaryClass(Class<?> ckass)
    {
        ClassModel classModel = new ClassModel(ckass.getSimpleName() + "$Mapper$" + count.getAndIncrement(), AbstractMapper.class, ckass);
        classModel.addImport(AbstractMapper.class);
        classModel.addImport(Template.class);
        classModel.addImport(Map.class);
        classModel.addImport(HashMap.class);
        classModel.addImport(String.class);
        classModel.addImport(BeanTransfer.class);
        classModel.addImport(SqlSession.class);
        classModel.addImport(List.class);
        return classModel;
    }

    private static TableEntityInfo getTableEntityInfoFromInterface(Class ckass, Map<String, TableEntityInfo> tableEntityInfos)
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
        TableEntityInfo tableEntityInfo = tableEntityInfos.get(entityClass.getSimpleName());
        if (tableEntityInfo == null)
        {
            throw new NullPointerException();
        }
        return tableEntityInfo;
    }

    private static TriTree getOperators()
    {
        TriTree operatrors = new TriTree();
        for (Operator each : Operator.values())
        {
            operatrors.set(each.name());
        }
        return operatrors;
    }

    private static TriTree getPropertyNames(TableEntityInfo tableEntityInfo)
    {
        TriTree propertyNames = new TriTree();
        for (String each : tableEntityInfo.getPropertyNameKeyMap().keySet())
        {
            String transferPropertyName = each.substring(0, 1).toUpperCase() + each.substring(1);
            propertyNames.set(transferPropertyName);
        }
        return propertyNames;
    }

    /**
     * 通过类似JPA的形式生成具体实现
     *
     * @return
     */
    private static String generateSqlByJpaMode(TableEntityInfo tableEntityInfo, TriTree operators, TriTree propertyNames, Method method)
    {
        String        methodName = method.getName();
        StringBuilder sql        = new StringBuilder();
        int           paramIndex = 0;
        int           length     = methodName.length();
        int           index      = 0;
        if (methodName.startsWith("find"))
        {
            sql.append("select * from ").append(tableEntityInfo.getTableName());
            index = 4;
        }
        else if (methodName.startsWith("count"))
        {
            sql.append("select count(*) from ").append(tableEntityInfo.getTableName());
            index = 4;
        }
        else if (methodName.startsWith("delete"))
        {
            sql.append("delete from ").append(tableEntityInfo.getTableName());
            index = 6;
        }
        else
        {
            sql.append("update ").append(tableEntityInfo.getTableName()).append(" set ");
            index = 6;
            boolean propertyNameNow = true;
            while (index < length)
            {
                if (propertyNameNow)
                {
                    String propertyName = propertyNames.find(methodName.substring(index));
                    if (propertyName == null)
                    {
                        throw new IllegalArgumentException("方法：" + method.toGenericString() + "无法找到[" + methodName.substring(index) + "]相匹配的属性名");
                    }
                    TableEntityInfo.ColumnInfo columnInfo = tableEntityInfo.getPropertyNameKeyMap().get(propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1));
                    sql.append(columnInfo.getColumnName()).append(" = ${name").append(paramIndex++).append("} ");
                    index += propertyName.length();
                    propertyNameNow = false;
                }
                else
                {
                    propertyNameNow = true;
                    String   operatorName = operators.find(methodName.substring(index));
                    Operator operator     = Operator.valueOf(operatorName);
                    if (operator == null)
                    {
                        break;
                    }
                    else if (operator == Operator.And)
                    {
                        sql.append(" , ");
                        index += operatorName.length();
                    }
                    else if (operator == Operator.By)
                    {
                        break;
                    }
                    else
                    {
                        throw new IllegalArgumentException("方法：" + method.toGenericString() + "方法名错误，当前是更新模式。更新结束只能是And，By两种");
                    }
                }
            }
        }
        sql.append(" where ");
        while (index < length)
        {
            String content      = methodName.substring(index);
            String operatorName = operators.find(content);
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
                        if (content.startsWith("Pk"))
                        {
                            propername = tableEntityInfo.getPkInfo().getPropertyName();
                            index += 2;
                        }
                        else
                        {
                            throw new IllegalArgumentException("方法：" + method.toGenericString() + "中：" + propername + "不匹配属性名");
                        }
                    }
                    else
                    {
                        index += propername.length();
                    }
                    propername = propername.substring(0, 1).toLowerCase() + propername.substring(1);
                    TableEntityInfo.ColumnInfo columnInfo = tableEntityInfo.getPropertyNameKeyMap().get(propername);
                    sql.append(columnInfo.getColumnName()).append(" ");
                    operatorName = operators.find(methodName.substring(index));
                    if (operatorName != null && (Operator.And.name().equals(operatorName) || Operator.Or.equals(operatorName) || Operator.OrderBy.name().equals(operatorName)))
                    {
                        sql.append(" = ${name").append(paramIndex).append("} ");
                        paramIndex++;
                    }
                    else if (index == length)
                    {
                        sql.append(" = ${name").append(paramIndex).append("} ");
                    }
                    break;
                }
                case And:
                case Or:
                {
                    sql.append(" ").append(operatorName.toLowerCase()).append(' ');
                    index += operatorName.length();
                    break;
                }
                case Before:
                case LessThan:
                {
                    sql.append(" < ${name").append(paramIndex).append("} ");
                    paramIndex++;
                    index += operatorName.length();
                    break;
                }
                case LessThanEqual:
                {
                    sql.append(" <= ${name").append(paramIndex).append("} ");
                    paramIndex++;
                    index += operatorName.length();
                    break;
                }
                case Like:
                {
                    sql.append(" like ${name").append(paramIndex).append("} ");
                    paramIndex++;
                    index += operatorName.length();
                    break;
                }
                case True:
                {
                    sql.append(" is true ");
                    index += operatorName.length();
                    break;
                }
                case False:
                {
                    sql.append(" is false ");
                    index += operatorName.length();
                    break;
                }
                case GreaterThan:
                case After:
                {
                    sql.append(" > ${name").append(paramIndex).append("} ");
                    paramIndex++;
                    index += operatorName.length();
                    break;
                }
                case In:
                {
                    sql.append(" in ~{name").append(paramIndex).append("} ");
                    paramIndex++;
                    index += operatorName.length();
                    break;
                }
                case NotIn:
                {
                    sql.append(" not in ~{name").append(paramIndex).append("} ");
                    paramIndex++;
                    index += operatorName.length();
                    break;
                }
                case IsNull:
                {
                    sql.append(" is null ");
                    index += operatorName.length();
                    break;
                }
                case Between:
                {
                    sql.append(" between ${name").append(paramIndex++).append("} and $name").append(paramIndex++).append("} ");
                    index += operatorName.length();
                    break;
                }
                case NotLike:
                {
                    sql.append(" not like ${name").append(paramIndex).append("} ");
                    paramIndex++;
                    index += operatorName.length();
                    break;
                }
                case IsNotNull:
                {
                    sql.append(" is not null ");
                    index += operatorName.length();
                    break;
                }
                case OrderBy:
                {
                    sql.append(" order by ");
                    index += operatorName.length();
                    String                     propertyName = propertyNames.find(methodName.substring(index));
                    TableEntityInfo.ColumnInfo columnInfo   = tableEntityInfo.getPropertyNameKeyMap().get(propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1));
                    sql.append(columnInfo.getColumnName());
                    index += propertyName.length();
                    String nextOperator = operators.find(methodName.substring(index));
                    if (Operator.Desc.name().equals(nextOperator) || Operator.Asc.name().equals(nextOperator))
                    {
                        sql.append(' ').append(nextOperator.toLowerCase()).append(' ');
                    }
                    else
                    {
                        throw new IllegalArgumentException("方法：" + method.toString() + "命名中,[" + methodName.substring(index - propertyName.length() - operatorName.length()) + "]部分，无法找到对应的属性名或者缺少排序指示Desc或Asc");
                    }
                    index += nextOperator.length();
                    break;
                }
                case Containing:
                {
                    sql.append(" like ${'%'+name").append(paramIndex++).append("+'%'} ");
                    index += operatorName.length();
                    break;
                }
                case EndingWith:
                {
                    sql.append(" like ${'%'+name").append(paramIndex++).append("} ");
                    index += operatorName.length();
                    break;
                }
                case StartingWith:
                {
                    sql.append(" like ${name").append(paramIndex++).append("+'%'} ");
                    index += operatorName.length();
                    break;
                }
                case GreaterThanEqual:
                {
                    sql.append(" >= ${name").append(paramIndex).append("} ");
                    paramIndex++;
                    index += operatorName.length();
                    break;
                }
                case Asc:
                case Desc:
                    throw new IllegalStateException("不应该解析到这个词");
            }
        }
        return sql.toString();
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
     * @return
     */
    private static String generateSqlAndTemplateField(Map<String, TableEntityInfo> tableEntityInfos, ClassModel classModel, AtomicInteger fieldNameCount, Method method, StringBuilder cache)
    {
        Sql        annotation        = method.getAnnotation(Sql.class);
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

    private static String generateSqlAndTemplateField(TableEntityInfo tableEntityInfo, TriTree operators, TriTree propertyNames, ClassModel classModel, AtomicInteger fieldNameCount, Method method, StringBuilder cache)
    {
        String     formatSql         = generateSqlByJpaMode(tableEntityInfo, operators, propertyNames, method);
        String     templateFieldName = "template_" + (fieldNameCount.getAndIncrement());
        FieldModel fieldModel        = new FieldModel(templateFieldName, Template.class, "Template.parse(\"" + formatSql + "\")", classModel);
        classModel.addField(fieldModel);
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 0)
        {
            int length = parameterTypes.length;
            for (int i = 0; i < length; i++)
            {
                cache.append("variables.put(\"name").append(i).append("\",$").append(i).append(");\r\n");
            }
        }
        cache.append("String sql =").append(templateFieldName).append(".render(variables,params);\r\n");
        if (parameterTypes.length != 0 && parameterTypes[parameterTypes.length - 1] == Page.class)
        {
            cache.append("params.add($").append(parameterTypes.length - 1).append(");\r\n");
        }
        return formatSql;
    }

    enum Operator
    {
        And, Or,//
        By,//
        Between, LessThan, LessThanEqual, GreaterThan, GreaterThanEqual, After, Before,//
        Like, NotLike, StartingWith, EndingWith, Containing, In, NotIn,//
        True, False, IsNull, IsNotNull, OrderBy, Desc, Asc
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
}
