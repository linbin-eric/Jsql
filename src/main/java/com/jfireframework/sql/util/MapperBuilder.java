package com.jfireframework.sql.util;

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
import com.jfireframework.baseutil.smc.compiler.JavaStringCompiler;
import com.jfireframework.baseutil.smc.model.CompilerModel;
import com.jfireframework.baseutil.smc.model.FieldModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.sql.annotation.EnumBoundHandler;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.metadata.TableMetaData.FieldInfo;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;
import com.jfireframework.sql.resultsettransfer.TransferHelper;
import com.jfireframework.sql.util.MapperBuilder.SqlContext.EnumHandlerInfo;
import com.jfireframework.sql.util.enumhandler.EnumHandler;
import com.jfireframework.sql.util.enumhandler.EnumStringHandler;
import com.jfireframework.sql.util.smc.DynamicCodeTool;

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
            CompilerModel compilerModel = DynamicCodeTool.createMapper(origin);
            createTargetClassMethod(compilerModel, origin);
            JavaStringCompiler compiler = new JavaStringCompiler();
            try
            {
                Class<?> result = compiler.compile(compilerModel, Thread.currentThread().getContextClassLoader());
                logger.debug("接口:{}编译的源代码是\r\n{}\r\n", origin.getName(), compilerModel.toString());
                return result;
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private void createTargetClassMethod(CompilerModel compilerModel, Class<?> interfaceCtClass) throws Exception
    {
        for (Method method : interfaceCtClass.getDeclaredMethods())
        {
            try
            {
                if (method.isAnnotationPresent(Sql.class))
                {
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
                else
                {
                    throw new UnsupportedOperationException(StringUtil.format("Mapper接口内不能存在非注解的方法。请检查{}.{}", method.getDeclaringClass().getName(), method.getName()));
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(StringUtil.format("接口存在错误，请检查{}.{}", method.getDeclaringClass().getName(), method.getName()), e);
            }
        }
        
    }
    
    private void createQueryMethod(CompilerModel compilerModel, Method method, String sql, String[] paramNames) throws Exception
    {
        SqlContext sqlContext = new SqlContext();
        boolean isPage = false;
        boolean isList = List.class.isAssignableFrom(method.getReturnType()) ? true : false;
        if (isList)
        {
            Verify.True(((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0].getClass().equals(Class.class), "方法{}.{}返回类型是泛型，不允许，请指定具体的类型", method.getDeclaringClass(), method.getName());
            Type returnParamType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
            // 确认方法放回不是List<T>的形式
            Verify.False(returnParamType instanceof WildcardType, "接口的返回类型不能是泛型，请检查{}.{}", method.getDeclaringClass().getName(), method.getName());
        }
        if (method.getParameterTypes().length > 0 && Page.class.isAssignableFrom(method.getParameterTypes()[method.getParameterTypes().length - 1]))
        {
            isPage = true;
        }
        boolean isDynamicSql = DynamicSqlTool.isDynamic(sql);
        StringCache methodBody = new StringCache(1024);
        methodBody.append("com.jfireframework.sql.session.SqlSession session = sessionFactory.getCurrentSession();\r\n");
        methodBody.append("if(session==null){throw new java.lang.NullPointerException(\"current session 为空，请检查\");}\r\n");
        if (isDynamicSql)
        {
            methodBody.append(DynamicSqlTool.analyseDynamicSql(sql, paramNames, method.getParameterTypes(), metaContext, sqlContext));
            if (isList)
            {
                Type returnParamType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                String fieldName = addResultsetTransferField(compilerModel, TransferHelper.buildInitStr((Class<?>) returnParamType, true));
                if (isPage)
                {
                    methodBody.append("return session.queryList(").append(fieldName)//
                            .append(",sql,$").append(method.getParameterTypes().length - 1).append(",list.toArray());");
                }
                else
                {
                    methodBody.append("return session.queryList(").append(fieldName)//
                            .append(",sql,list.toArray());");
                }
            }
            else
            {
                Class<?> returnType = method.getReturnType();
                String fieldName = addResultsetTransferField(compilerModel, TransferHelper.buildInitStr(returnType, true));
                methodBody.append("return (" + returnType.getName() + ")session.query(").append(fieldName)//
                        .append(",sql,list.toArray());");
            }
        }
        else
        {
            DynamicSqlTool.analyseFormatSql(sql, paramNames, method.getParameterTypes(), metaContext, sqlContext);
            if (isList)
            {
                Type returnParamType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                String fieldName = addResultsetTransferField(compilerModel, TransferHelper.buildInitStr((Class<?>) returnParamType, false));
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
                String fieldName = addResultsetTransferField(compilerModel, TransferHelper.buildInitStr(returnType, false));
                methodBody.append("return (" + returnType.getName() + ")session.query(").append(fieldName).append(",\"")//
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
            FieldModel fieldModel = new FieldModel(each.getName(), each.getHandlerType(), StringUtil.format("new {}({}.class)", each.getHandlerType().getName(), each.getType().getName()));
            compilerModel.addField(fieldModel);
        }
    }
    
    private String addResultsetTransferField(CompilerModel compilerModel, String initStr)
    {
        String fieldName = "transferField_" + fieldNo;
        fieldNo++;
        compilerModel.addField(new FieldModel(fieldName, ResultSetTransfer.class, initStr));
        return fieldName;
    }
    
    private void createUpdateMethod(CompilerModel compilerModel, Method method, String sql, String[] paramNames) throws Exception
    {
        SqlContext sqlContext = new SqlContext();
        StringCache cache = new StringCache(1024);
        cache.append("com.jfireframework.sql.session.SqlSession session = sessionFactory.getCurrentSession();\r\n");
        cache.append("if(session==null){throw new NullPointerException(\"current session 为空，请检查\");}\r\n");
        boolean isDynamicSql = DynamicSqlTool.isDynamic(sql);
        if (isDynamicSql)
        {
            cache.append(DynamicSqlTool.analyseDynamicSql(sql, paramNames, method.getParameterTypes(), metaContext, sqlContext));
            cache.append("int updateRows=0;\r\n");
            cache.append("if(list.size()==0){\r\n");
            cache.append("updateRows = session.update(sql,emptyParams);}\r\n");
            cache.append("else{\r\n");
            cache.append("updateRows = session.update(sql,list.toArray());\r\n");
            cache.append("}\r\n");
        }
        else
        {
            DynamicSqlTool.analyseFormatSql(sql, paramNames, method.getParameterTypes(), metaContext, sqlContext);
            cache.append("int updateRows = session.update(\"").append(sqlContext.getSql()).append("\",");
            if (sqlContext.getQueryParams().isEmpty())
            {
                cache.append("emptyParams);\r\n");
            }
            else
            {
                cache.append("new Object[]{");
                for (String each : sqlContext.getQueryParams())
                {
                    cache.append(each).appendComma();
                }
                cache.deleteLast().append("});\r\n");
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
        private String                countSql;
        private List<String>          queryParams      = new LinkedList<String>();
        private List<EnumHandlerInfo> enumHandlerInfos = new LinkedList<MapperBuilder.SqlContext.EnumHandlerInfo>();
        
        public boolean hasMetaContext()
        {
            return !metaContexts.isEmpty();
        }
        
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
        
        public String getCountSql()
        {
            return countSql;
        }
        
        public void setCountSql(String countSql)
        {
            this.countSql = countSql;
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
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
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
                try
                {
                    EnumHandler<?> enumHandler = ckass.getConstructor(Class.class).newInstance(fieldType);
                    for (Enum<?> enumInstance : ReflectUtil.getAllEnumInstances(fieldType).values())
                    {
                        staticValueMap.put(fieldType.getSimpleName() + "." + enumInstance.name(), enumHandler.getValue(enumInstance));
                        staticValueMap.put(enumInstance.name(), enumHandler.getValue(enumInstance));
                    }
                }
                catch (Exception e)
                {
                    throw new JustThrowException(e);
                }
                
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
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
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
                try
                {
                    EnumHandler<?> enumHandler = ckass.getConstructor(Class.class).newInstance(fieldType);
                    for (Enum<?> enumInstance : ReflectUtil.getAllEnumInstances(fieldType).values())
                    {
                        staticValueMap.put(fieldType.getSimpleName() + "." + enumInstance.name(), enumHandler.getValue(enumInstance));
                        staticValueMap.put(enumInstance.name(), enumHandler.getValue(enumInstance));
                    }
                }
                catch (Exception e)
                {
                    throw new JustThrowException(e);
                }
                
            }
        }
        
        public static class EnumHandlerInfo
        {
            private Class<? extends Enum<?>>        type;
            private Class<? extends EnumHandler<?>> handlerType;
            private String                          name;
            
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
            
            public void setType(Class<? extends Enum<?>> type)
            {
                this.type = type;
            }
            
            public Class<? extends EnumHandler<?>> getHandlerType()
            {
                return handlerType;
            }
            
            public void setHandlerType(Class<? extends EnumHandler<?>> handlerType)
            {
                this.handlerType = handlerType;
            }
            
            public String getName()
            {
                return name;
            }
            
            public void setName(String name)
            {
                this.name = name;
            }
            
        }
        
        public void addEnumHandler(String name, Class<? extends Enum<?>> enumType, Class<? extends EnumHandler<?>> handleType)
        {
            enumHandlerInfos.add(new EnumHandlerInfo(name, enumType, handleType));
        }
        
        public List<EnumHandlerInfo> enumHandlerInfos()
        {
            return enumHandlerInfos;
        }
        
        public String getDbColName(String fieldName)
        {
            return dbColNameMap.get(fieldName);
        }
        
        public String getFieldName(String dbColName)
        {
            return fieldNameMap.get(dbColName);
        }
        
        public Object getStaticValue(String name)
        {
            return staticValueMap.get(name);
        }
        
    }
    
    /**
     * 用来存储调用的方法源代码，和最终的表达式返回的类型。
     * 比如存储类似$1.getName()这样的表达式，和String.class这样的该方法调用都返回值
     *
     * @author 林斌
     *
     */
    public static class InvokeNameAndType
    {
        private final String   origin;
        private final String   invokeName;
        private final Class<?> returnType;
        
        public InvokeNameAndType(String invokeName, Class<?> returnType, String origin)
        {
            this.invokeName = invokeName;
            this.returnType = returnType;
            this.origin = origin;
        }
        
        public String getOrigin()
        {
            return origin;
        }
        
        public String getInvokeName()
        {
            return invokeName;
        }
        
        public Class<?> getReturnType()
        {
            return returnType;
        }
        
    }
}
