package com.jfirer.jsql.mapper;

import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.bytecode.support.SupportOverrideAttributeAnnotationContextFactory;
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
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.Param;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.transfer.impl.BeanTransfer;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MapperGenerator
{
    private static final AtomicInteger                                            count                    = new AtomicInteger(0);
    private static       AnnotationContextFactory                                 annotationContextFactory = new SupportOverrideAttributeAnnotationContextFactory();
    private static       CompileHelper                                            compileHelper            = new CompileHelper(Thread.currentThread().getContextClassLoader());
    private static       ConcurrentMap<Class<?>, Class<? extends AbstractMapper>> store                    = new ConcurrentHashMap<>();

    public static Class<? extends AbstractMapper> generate(Class<?> ckass)
    {
        if (annotationContextFactory.get(ckass).isAnnotationPresent(Mapper.class) == false)
        {
            throw new IllegalArgumentException();
        }
        return store.computeIfAbsent(ckass, v -> (Class<? extends AbstractMapper>) generate(v, compileHelper));
    }

    private static Class<?> generate(Class<?> ckass, CompileHelper compiler)
    {
        try
        {
            ClassModel                   classModel       = buildClassModelAndImportNecessaryClass(ckass);
            AtomicInteger                fieldNameCount   = new AtomicInteger(0);
            Map<String, TableEntityInfo> tableEntityInfos = new HashMap<>();
            Arrays.stream(annotationContextFactory.get(ckass).getAnnotation(Mapper.class).value()).map(value -> TableEntityInfo.parse(value)).forEach(entityInfo -> {
                tableEntityInfos.put(entityInfo.getClassSimpleName(), entityInfo);
            });
            for (Method method : ckass.getDeclaredMethods())
            {
                if (method.isDefault() || method.isAnnotationPresent(Sql.class) == false)
                {
                    continue;
                }
                StringBuilder methodBody = new StringBuilder();
                methodBody.append("if(session==null){throw new NullPointerException(\"当前没有session\");}");
                methodBody.append("Map<String,Object> variables = cachedVariables.get();\r\n");
                methodBody.append("List<Object> params = cachedParams.get();\r\n");
                methodBody.append("try{\r\n");
                MethodModel methodModel = new MethodModel(method, classModel);
                String      formatSql   = generateSqlAndTemplateField(tableEntityInfos, classModel, fieldNameCount, method, methodBody);
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
                    methodBody.append("int result = session.execute(sql,params);\r\n");
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
            if (Repository.class.isAssignableFrom(ckass))
            {
                Class<?> repositoryEntityClass = null;
                for (Type genericInterface : ckass.getGenericInterfaces())
                {
                    if (genericInterface instanceof ParameterizedType parameterizedType)
                    {
                        repositoryEntityClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                    }
                }
                Objects.requireNonNull(repositoryEntityClass);
                classModel.addImport(repositoryEntityClass);
                classModel.addImport(Param.class);
                classModel.addImport(Model.class);
                addFindOne(classModel, (Class<? extends Repository>) ckass, repositoryEntityClass);
                addFindList(classModel, (Class<? extends Repository>) ckass, repositoryEntityClass);
                addDelete(classModel, (Class<? extends Repository>) ckass, repositoryEntityClass);
                addInsert(classModel, (Class<? extends Repository>) ckass, repositoryEntityClass);
                addSave(classModel, (Class<? extends Repository>) ckass, repositoryEntityClass);
                addUpadte(classModel, (Class<? extends Repository>) ckass, repositoryEntityClass);
                addCount(ckass, classModel, repositoryEntityClass);
            }
            return compiler.compile(classModel);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    private static void addCount(Class<?> ckass, ClassModel classModel, Class<?> repositoryEntityClass) throws NoSuchMethodException
    {
        Method      count       = ckass.getMethod("count", Param.class);
        MethodModel methodModel = new MethodModel(count, classModel);
        methodModel.setBody("""
                            if(session==null){throw new NullPointerException("当前没有session");}
                            return session.count(Model.selectCount().from(
                            """ + SmcHelper.getReferenceName(repositoryEntityClass, classModel) + ".class).where($0));");
        classModel.putMethodModel(methodModel);
    }

    private static void addUpadte(ClassModel classModel, Class<? extends Repository> ckass, Class<?> repositoryEntityClass) throws NoSuchMethodException
    {
        Method      update      = ckass.getMethod("update", Object.class);
        MethodModel methodModel = new MethodModel(update, classModel);
        methodModel.setParamterTypes(repositoryEntityClass);
        methodModel.setBody("""
                            if(session==null){throw new NullPointerException("当前没有session");}
                            return session.update($0);
                            """);
        classModel.putMethodModel(methodModel);
    }

    private static void addSave(ClassModel classModel, Class<? extends Repository> ckass, Class<?> repositoryEntityClass) throws NoSuchMethodException
    {
        Method      save        = ckass.getMethod("save", Object.class);
        MethodModel methodModel = new MethodModel(save, classModel);
        methodModel.setParamterTypes(repositoryEntityClass);
        methodModel.setBody("""
                            if(session==null){throw new NullPointerException("当前没有session");}
                            return session.save($0);
                            """);
        classModel.putMethodModel(methodModel);
    }

    private static void addInsert(ClassModel classModel, Class<? extends Repository> ckass, Class<?> repositoryEntityClass) throws NoSuchMethodException
    {
        Method      insert      = ckass.getMethod("insert", Object.class);
        MethodModel methodModel = new MethodModel(insert, classModel);
        methodModel.setParamterTypes(repositoryEntityClass);
        methodModel.setBody("""
                            if(session==null){throw new NullPointerException("当前没有session");}
                            return session.insert($0);
                            """);
        classModel.putMethodModel(methodModel);
    }

    private static void addDelete(ClassModel classModel, Class<? extends Repository> ckass, Class<?> repositoryEntityClass) throws NoSuchMethodException
    {
        Method      delete      = ckass.getMethod("delete", Param.class);
        MethodModel methodModel = new MethodModel(delete, classModel);
        methodModel.setBody("""
                            if(session==null){throw new NullPointerException("当前没有session");}
                            return session.execute(Model.deleteFrom(""" + SmcHelper.getReferenceName(repositoryEntityClass, classModel) + ".class).where($0));");
        classModel.putMethodModel(methodModel);
    }

    private static void addFindList(ClassModel classModel, Class<? extends Repository> ckass, Class<?> repositoryEntityClass) throws NoSuchMethodException
    {
        Method      findList    = ckass.getMethod("findList", Param.class);
        MethodModel methodModel = new MethodModel(findList, classModel);
        methodModel.setBody("""
                            if(session==null){throw new NullPointerException("当前没有session");}
                            return session.findList(Model.selectAll().from(""" //
                            + SmcHelper.getReferenceName(repositoryEntityClass, classModel) + ".class).where($0));");
        classModel.putMethodModel(methodModel);
    }

    private static void addFindOne(ClassModel classModel, Class<? extends Repository> ckass, Class<?> repositoryEntityClass) throws NoSuchMethodException
    {
        Method      method  = ckass.getMethod("findOne", Param.class);
        MethodModel findOne = new MethodModel(method, classModel);
        findOne.setReturnType(repositoryEntityClass);
        findOne.setBody("""
                        if(session==null){throw new NullPointerException("当前没有session");}
                        return session.findOne(Model.selectAll().from(""" //
                        + SmcHelper.getReferenceName(repositoryEntityClass, classModel) + ".class).where($0));");
        classModel.putMethodModel(findOne);
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
