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
import com.jfirer.jsql.transfer.impl.BeanTransfer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MapperGenerator
{
    private static final AtomicInteger count = new AtomicInteger(0);

    public static Class<?> generate(Class<?> ckass, Map<String, TableEntityInfo> tableEntityInfos, CompileHelper compiler)
    {
        ClassModel    classModel     = buildClassModelAndImportNecessaryClass(ckass);
        AtomicInteger fieldNameCount = new AtomicInteger(0);
        for (Method method : ckass.getDeclaredMethods())
        {
            if (method.isDefault())
            {
                continue;
            }
            StringBuilder methodBody = new StringBuilder();
            methodBody.append("if(session==null){throw new NullPointerException(\"当前没有session\");}");
            methodBody.append("Map<String,Object> variables = cachedVariables.get();\r\n");
            methodBody.append("List<Object> params = cachedParams.get();\r\n");
            methodBody.append("try{\r\n");
            MethodModel methodModel = new MethodModel(method, classModel);
            String      formatSql;
            if (method.isAnnotationPresent(Sql.class))
            {
                formatSql = generateSqlAndTemplateField(tableEntityInfos, classModel, fieldNameCount, method, methodBody);
            }
            else
            {
                throw new IllegalArgumentException(method.toString());
            }
            if (formatSql.startsWith("SELECT") || formatSql.startsWith("select"))
            {
                int methodIndex = AbstractMapper.put(method);
                if (List.class.isAssignableFrom(method.getReturnType()))
                {
                    methodBody.append("List result = session.queryList(sql,methods.get(").append(methodIndex).append("),params);\r\n");
                }
                else
                {
                    String returnTypeName = method.getReturnType().isPrimitive() ? ReflectUtil.wrapPrimitive(method.getReturnType()).getName() : SmcHelper.getReferenceName(method.getReturnType(), classModel);
                    methodBody.append(returnTypeName).append(" result = session.query(sql,methods.get(").append(methodIndex).append("),params);\r\n");
                }
            }
            else
            {
                methodBody.append("int result = session.update(sql,params);\r\n");
            }
            if (method.getReturnType() == void.class || method.getReturnType() == Void.class)
            {
                ;
            }
            else
            {
                methodBody.append("return result;\r\n");
            }
            methodBody.append("}finally {params.clear();variables.clear();}");
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
}
