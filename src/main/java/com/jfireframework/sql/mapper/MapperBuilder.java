package com.jfireframework.sql.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.compiler.JavaStringCompiler;
import com.jfireframework.baseutil.smc.model.CompilerModel;
import com.jfireframework.baseutil.smc.model.FieldModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.sql.annotation.EnumBoundHandler;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.mapper.MapperBuilder.SqlContext.EnumHandlerInfo;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.metadata.TableMetaData.FieldInfo;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;
import com.jfireframework.sql.resultsettransfer.TransferFactory;
import com.jfireframework.sql.util.enumhandler.EnumHandler;
import com.jfireframework.sql.util.enumhandler.EnumStringHandler;

public class MapperBuilder
{
    private final MetaContext   metaContext;
    private int                 fieldNo = 1;
    private static final Logger logger  = LoggerFactory.getLogger(MapperBuilder.class);
    
    public MapperBuilder(MetaContext metaContext)
    {
        this.metaContext = metaContext;
    }
    
    /**
     * 创造一个Mapper的子类，该子类同时实现了用户指定的接口。并且接口的实现内容就是对注解的sql语句的执行
     *
     * @param interfaceClass 子类需要实现的接口
     * @return
     */
    public Class<?> build(Class<?> origin)
    {
        try
        {
            CompilerModel compilerModel = SmcHelper.createImplClass(Mapper.class, origin);
            createTargetClassMethod(compilerModel, origin);
            JavaStringCompiler compiler = new JavaStringCompiler();
            Class<?> result = compiler.compile(compilerModel, Thread.currentThread().getContextClassLoader());
            logger.debug("接口:{}编译的源代码是\r\n{}\r\n", origin.getName(), compilerModel.toString());
            return result;
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
    }
    
    private void createTargetClassMethod(CompilerModel compilerModel, Class<?> interfaceCtClass) throws Exception
    {
        for (Method method : interfaceCtClass.getDeclaredMethods())
        {
            try
            {
                if (method.isAnnotationPresent(Sql.class) == false)
                {
                    throw new UnsupportedOperationException(StringUtil.format("Mapper接口内不能存在非注解的方法。请检查{}.{}", method.getDeclaringClass().getName(), method.getName()));
                }
                Sql sql = method.getAnnotation(Sql.class);
                if (sql.sql().startsWith("select"))
                {
                    createQueryMethod(compilerModel, method, sql.sql(), sql.paramNames().split(","));
                }
                else
                {
                    createUpdateMethod(compilerModel, method, sql.sql(), sql.paramNames().split(","));
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(StringUtil.format("接口存在错误，请检查{}.{}", method.getDeclaringClass().getName(), method.getName()), e);
            }
        }
        
    }
    
    private boolean detectIsList(Method method)
    {
        boolean isList = List.class.isAssignableFrom(method.getReturnType()) ? true : false;
        if (isList)
        {
            Verify.True(((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0].getClass().equals(Class.class), "方法{}.{}返回类型是泛型，不允许，请指定具体的类型", method.getDeclaringClass(), method.getName());
            Type returnParamType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
            // 方法返回不能是泛型
            Verify.False(returnParamType instanceof WildcardType, "接口的返回类型不能是泛型，请检查{}.{}", method.getDeclaringClass().getName(), method.getName());
        }
        return isList;
    }
    
    private boolean detectIsPage(Method method)
    {
        if (method.getParameterTypes().length > 0 && Page.class.isAssignableFrom(method.getParameterTypes()[method.getParameterTypes().length - 1]))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private void createQueryMethod(CompilerModel compilerModel, Method method, String sql, String[] paramNames) throws Exception
    {
        SqlContext sqlContext = new SqlContext();
        boolean isPage = detectIsPage(method);
        boolean isList = detectIsList(method);
        boolean isDynamicSql = SqlTextAnalyse.isDynamic(sql);
        StringCache methodBody = new StringCache(1024);
        methodBody.append("com.jfireframework.sql.session.SqlSession session = sessionFactory.getCurrentSession();\r\n");
        methodBody.append("if(session==null){throw new java.lang.NullPointerException(\"current session 为空，请检查\");}\r\n");
        if (isDynamicSql)
        {
            methodBody.append(SqlTextAnalyse.analyseDynamicText(sql, paramNames, method.getParameterTypes(), metaContext, sqlContext));
            if (isList)
            {
                Type returnParamType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                String fieldName = addResultsetTransferField(compilerModel, (Class<?>) returnParamType);
                if (isPage)
                {
                    String pageParamName = "$" + (method.getParameterTypes().length - 1);
                    methodBody//
                            .append("return session.queryList(")//
                            .append(fieldName).appendComma()//
                            .append("sql").appendComma()//
                            .append(pageParamName)//
                            .appendComma().append("list.toArray()")//
                            .append(");");
                }
                else
                {
                    methodBody.append("return session.queryList(")//
                            .append(fieldName).appendComma()//
                            .append("sql").appendComma()//
                            .append("list.toArray()")//
                            .append(");");
                }
            }
            else
            {
                Class<?> returnType = method.getReturnType();
                String fieldName = addResultsetTransferField(compilerModel, returnType);
                methodBody.append("return (" + SmcHelper.getTypeName(returnType) + ")session.query(")//
                        .append(fieldName).append(",sql,list.toArray());");
            }
        }
        else
        {
            SqlTextAnalyse.analyseStaticText(sql, paramNames, method.getParameterTypes(), metaContext, sqlContext);
            if (isList)
            {
                Type returnParamType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                String fieldName = addResultsetTransferField(compilerModel, (Class<?>) returnParamType);
                if (isPage)
                {
                    methodBody.append("return session.queryList(").append(fieldName).append(",\"")//
                            .append(sqlContext.getSql()).append("\",$").append(method.getParameterTypes().length - 1).append(",");
                }
                else
                {
                    methodBody.append("return session.queryList(").append(fieldName).append(",\"")//
                            .append(sqlContext.getSql()).append("\",");
                }
            }
            else
            {
                Class<?> returnType = method.getReturnType();
                String fieldName = addResultsetTransferField(compilerModel, returnType);
                methodBody.append("return (" + SmcHelper.getTypeName(returnType) + ")session.query(").append(fieldName).append(",\"")//
                        .append(sqlContext.getSql()).append("\",");
            }
            if (sqlContext.getQueryParams().size() == 0)
            {
                methodBody.append("emptyParams);");
            }
            else
            {
                methodBody.append("new Object[]{");
                for (String each : sqlContext.getQueryParams())
                {
                    methodBody.append(each).appendComma();
                }
                methodBody.deleteLast().append("});");
            }
        }
        MethodModel methodModel = new MethodModel(method);
        methodModel.setBody(methodBody.toString());
        compilerModel.putMethod(method, methodModel);
        createEnumBoundHandlerField(sqlContext, compilerModel);
    }
    
    private void createEnumBoundHandlerField(SqlContext sqlContext, CompilerModel compilerModel)
    {
        for (EnumHandlerInfo each : sqlContext.enumHandlerInfos)
        {
            FieldModel fieldModel = new FieldModel(each.getName(), each.getHandlerType(), StringUtil.format("new {}({}.class)", each.getHandlerType().getName(), SmcHelper.getTypeName(each.getType())));
            compilerModel.addField(fieldModel);
        }
    }
    
    private String addResultsetTransferField(CompilerModel compilerModel, Class<?> type)
    {
        String fieldName = "transferField_" + fieldNo;
        fieldNo++;
        String initStr = buildInitStr(type);
        compilerModel.addField(new FieldModel(fieldName, ResultSetTransfer.class, initStr));
        return fieldName;
    }
    
    @SuppressWarnings("rawtypes")
    private String buildInitStr(Class<?> type)
    {
        Class<? extends ResultSetTransfer> transfer = TransferFactory.get(type);
        return "new " + SmcHelper.getTypeName(transfer) + "(" + SmcHelper.getTypeName(type) + ".class)";
    }
    
    private void createUpdateMethod(CompilerModel compilerModel, Method method, String sql, String[] paramNames) throws Exception
    {
        SqlContext sqlContext = new SqlContext();
        StringCache cache = new StringCache(1024);
        cache.append("com.jfireframework.sql.session.SqlSession session = sessionFactory.getCurrentSession();\r\n");
        cache.append("if(session==null){throw new NullPointerException(\"current session 为空，请检查\");}\r\n");
        boolean isDynamicSql = SqlTextAnalyse.isDynamic(sql);
        if (isDynamicSql)
        {
            cache.append(SqlTextAnalyse.analyseDynamicText(sql, paramNames, method.getParameterTypes(), metaContext, sqlContext));
            cache.append("int updateRows=session.update(sql,emptyParams);\r\n");
        }
        else
        {
            SqlTextAnalyse.analyseStaticText(sql, paramNames, method.getParameterTypes(), metaContext, sqlContext);
            cache.append("int updateRows = session.update(\"").append(sqlContext.getSql()).append("\",");
            if (sqlContext.getQueryParams().isEmpty())
            {
                cache.append("emptyParams);");
            }
            else
            {
                cache.append("new Object[]{");
                for (String each : sqlContext.getQueryParams())
                {
                    cache.append(each).appendComma();
                }
                cache.deleteLast().append("});");
            }
        }
        Class<?> returnType = method.getReturnType();
        if (returnType == Void.class || returnType == void.class)
        {
            ;
        }
        else
        {
            cache.append("return updateRows;\r\n");
        }
        MethodModel methodModel = new MethodModel(method);
        methodModel.setBody(cache.toString());
        compilerModel.putMethod(method, methodModel);
        createEnumBoundHandlerField(sqlContext, compilerModel);
    }
    
    public static class SqlContext
    {
        private List<String>          injectNames      = new LinkedList<String>();
        private Set<TableMetaData>    metaContexts     = new HashSet<TableMetaData>();
        private Map<String, String>   dbColNameMap     = new HashMap<String, String>();
        private Map<String, String>   fieldNameMap     = new HashMap<String, String>();
        private Map<String, Object>   staticValueMap   = new HashMap<String, Object>();
        private String                sql;
        private List<String>          queryParams      = new LinkedList<String>();
        private List<EnumHandlerInfo> enumHandlerInfos = new LinkedList<MapperBuilder.SqlContext.EnumHandlerInfo>();
        
        public List<String> getInjectNames()
        {
            return injectNames;
        }
        
        public void addInjectName(String inject)
        {
            injectNames.add(inject);
        }
        
        public List<String> getQueryParams()
        {
            return queryParams;
        }
        
        public void setQueryParams(List<String> queryParams)
        {
            this.queryParams = queryParams;
        }
        
        public String getSql()
        {
            return sql;
        }
        
        public void setSql(String sql)
        {
            this.sql = sql;
        }
        
        @SuppressWarnings("unchecked")
        public void addMetaData(TableMetaData metaData)
        {
            if (metaData == null)
            {
                throw new NullPointerException();
            }
            if (metaContexts.add(metaData) == false)
            {
                return;
            }
            Class<?> type = metaData.getEntityClass();
            String prefix = type.getSimpleName() + '.';
            String tablePrefix = metaData.getTableName() + ".";
            for (FieldInfo each : metaData.getFieldInfos())
            {
                dbColNameMap.put(each.getFieldName(), tablePrefix + each.getDbColName());
                dbColNameMap.put(prefix + each.getFieldName(), tablePrefix + each.getDbColName());
                fieldNameMap.put(tablePrefix + each.getDbColName(), each.getFieldName());
            }
            try
            {
                for (Entry<String, Field> each : metaData.staticFieldMap().entrySet())
                {
                    staticValueMap.put(prefix + each.getKey(), each.getValue().get(null));
                    staticValueMap.put(each.getKey(), each.getValue().get(null));
                }
                for (Entry<String, Field> each : metaData.enumFieldMap().entrySet())
                {
                    Class<? extends Enum<?>> fieldType = (Class<? extends Enum<?>>) each.getValue().getType();
                    Class<? extends EnumHandler<?>> ckass = null;
                    if (fieldType.isAnnotationPresent(EnumBoundHandler.class))
                    {
                        ckass = fieldType.getAnnotation(EnumBoundHandler.class).value();
                    }
                    else
                    {
                        ckass = EnumStringHandler.class;
                    }
                    EnumHandler<?> enumHandler = ckass.getConstructor(Class.class).newInstance(fieldType);
                    for (Enum<?> enumInstance : ReflectUtil.getAllEnumInstances(fieldType).values())
                    {
                        staticValueMap.put(fieldType.getSimpleName() + "." + enumInstance.name(), enumHandler.getValue(enumInstance));
                        staticValueMap.put(enumInstance.name(), enumHandler.getValue(enumInstance));
                    }
                }
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        
        @SuppressWarnings("unchecked")
        public void addAliasName(String name, TableMetaData metaData)
        {
            if (metaData == null)
            {
                throw new NullPointerException();
            }
            String prefix = name + '.';
            for (FieldInfo each : metaData.getFieldInfos())
            {
                dbColNameMap.put(each.getFieldName(), prefix + each.getDbColName());
                dbColNameMap.put(prefix + each.getFieldName(), prefix + each.getDbColName());
                fieldNameMap.put(prefix + each.getDbColName(), each.getFieldName());
            }
            try
            {
                for (Entry<String, Field> each : metaData.staticFieldMap().entrySet())
                {
                    staticValueMap.put(prefix + each.getKey(), each.getValue().get(null));
                    staticValueMap.put(each.getKey(), each.getValue().get(null));
                }
                for (Entry<String, Field> each : metaData.enumFieldMap().entrySet())
                {
                    Class<? extends Enum<?>> fieldType = (Class<? extends Enum<?>>) each.getValue().getType();
                    Class<? extends EnumHandler<?>> ckass = null;
                    if (fieldType.isAnnotationPresent(EnumBoundHandler.class))
                    {
                        ckass = fieldType.getAnnotation(EnumBoundHandler.class).value();
                    }
                    else
                    {
                        ckass = EnumStringHandler.class;
                    }
                    EnumHandler<?> enumHandler = ckass.getConstructor(Class.class).newInstance(fieldType);
                    for (Enum<?> enumInstance : ReflectUtil.getAllEnumInstances(fieldType).values())
                    {
                        staticValueMap.put(fieldType.getSimpleName() + "." + enumInstance.name(), enumHandler.getValue(enumInstance));
                        staticValueMap.put(enumInstance.name(), enumHandler.getValue(enumInstance));
                    }
                }
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        
        public static class EnumHandlerInfo
        {
            private final Class<? extends Enum<?>>        type;
            private final Class<? extends EnumHandler<?>> handlerType;
            private final String                          name;
            
            public EnumHandlerInfo(String name, Class<? extends Enum<?>> type, Class<? extends EnumHandler<?>> handlerType)
            {
                this.name = name;
                this.type = type;
                this.handlerType = handlerType;
            }
            
            public Class<? extends Enum<?>> getType()
            {
                return type;
            }
            
            public Class<? extends EnumHandler<?>> getHandlerType()
            {
                return handlerType;
            }
            
            public String getName()
            {
                return name;
            }
            
        }
        
        public void addEnumHandler(String name, Class<? extends Enum<?>> enumType, Class<? extends EnumHandler<?>> handleType)
        {
            enumHandlerInfos.add(new EnumHandlerInfo(name, enumType, handleType));
        }
        
        public String getDbColName(String fieldName)
        {
            return dbColNameMap.get(fieldName);
        }
        
        public Object getStaticValue(String name)
        {
            return staticValueMap.get(name);
        }
        
    }
    
}
