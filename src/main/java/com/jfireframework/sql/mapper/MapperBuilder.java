package com.jfireframework.sql.mapper;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.compiler.JavaStringCompiler;
import com.jfireframework.baseutil.smc.model.CompilerModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.resultsettransfer.ResultsetTransferStore;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class MapperBuilder
{
    private final JdbcTypeDictionary     jdbcTypeDictionary;
    private final MetaContext            metaContext;
    private final ResultsetTransferStore resultsetTransferStore;
    private static final Logger          logger = LoggerFactory.getLogger(MapperBuilder.class);
    
    public MapperBuilder(MetaContext metaContext, ResultsetTransferStore resultsetTransferStore, JdbcTypeDictionary jdbcTypeDictionary)
    {
        this.metaContext = metaContext;
        this.resultsetTransferStore = resultsetTransferStore;
        this.jdbcTypeDictionary = jdbcTypeDictionary;
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
                int sn = resultsetTransferStore.registerTransfer(method, jdbcTypeDictionary);
                if (isPage)
                {
                    String pageParamName = "$" + (method.getParameterTypes().length - 1);
                    methodBody//
                            .append("return session.queryList(sessionFactory.getResultSetTransferStore().get(")//
                            .append(sn).append(')').appendComma()//
                            .append("sql").appendComma()//
                            .append(pageParamName)//
                            .appendComma().append("list.toArray()")//
                            .append(");");
                }
                else
                {
                    methodBody.append("return session.queryList(sessionFactory.getResultSetTransferStore().get(")//
                            .append(sn).append(')').appendComma()//
                            .append("sql").appendComma()//
                            .append("list.toArray()")//
                            .append(");");
                }
            }
            else
            {
                Class<?> returnType = method.getReturnType();
                int sn = resultsetTransferStore.registerTransfer(method, jdbcTypeDictionary);
                methodBody.append("return (" + SmcHelper.getTypeName(returnType) + ")session.query(sessionFactory.getResultSetTransferStore().get(")//
                        .append(sn).append(')').append(",sql,list.toArray());");
            }
        }
        else
        {
            SqlTextAnalyse.analyseStaticText(sql, paramNames, method.getParameterTypes(), metaContext, sqlContext);
            if (isList)
            {
                int sn = resultsetTransferStore.registerTransfer(method, jdbcTypeDictionary);
                if (isPage)
                {
                    methodBody.append("return session.queryList(sessionFactory.getResultSetTransferStore().get(").append(sn).append(')').append(",\"")//
                            .append(sqlContext.getSql()).append("\",$").append(method.getParameterTypes().length - 1).append(",");
                }
                else
                {
                    methodBody.append("return session.queryList(sessionFactory.getResultSetTransferStore().get(").append(sn).append(')').append(",\"")//
                            .append(sqlContext.getSql()).append("\",");
                }
            }
            else
            {
                Class<?> returnType = method.getReturnType();
                int sn = resultsetTransferStore.registerTransfer(method, jdbcTypeDictionary);
                methodBody.append("return (" + SmcHelper.getTypeName(returnType) + ")session.query(sessionFactory.getResultSetTransferStore().get(").append(sn).append(')').append(",\"")//
                        .append(sqlContext.getSql()).append("\",");
            }
            if (sqlContext.getParams().size() == 0)
            {
                methodBody.append("emptyParams);");
            }
            else
            {
                methodBody.append("new Object[]{");
                for (String each : sqlContext.getParams())
                {
                    methodBody.append(each).appendComma();
                }
                methodBody.deleteLast().append("});");
            }
        }
        MethodModel methodModel = new MethodModel(method);
        methodModel.setBody(methodBody.toString());
        compilerModel.putMethod(method, methodModel);
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
            if (sqlContext.getParams().isEmpty())
            {
                cache.append("emptyParams);");
            }
            else
            {
                cache.append("new Object[]{");
                for (String each : sqlContext.getParams())
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
    }
    
    public static class SqlContext
    {
        private Set<TableMetaData>  metaContexts = new HashSet<TableMetaData>();
        private Map<String, String> dbColNameMap = new HashMap<String, String>();
        private Map<String, String> fieldNameMap = new HashMap<String, String>();
        private String              sql;
        private List<String>        params       = new LinkedList<String>();
        
        public List<String> getParams()
        {
            return params;
        }
        
        public void addParams(String param)
        {
            params.add(param);
        }
        
        public String getSql()
        {
            return sql;
        }
        
        public void setSql(String sql)
        {
            this.sql = sql;
        }
        
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
            for (MapField each : metaData.getFieldInfos())
            {
                dbColNameMap.put(each.getFieldName(), tablePrefix + each.getColName());
                dbColNameMap.put(prefix + each.getFieldName(), tablePrefix + each.getColName());
                fieldNameMap.put(tablePrefix + each.getColName(), each.getFieldName());
            }
        }
        
        public void addAliasName(String name, TableMetaData metaData)
        {
            if (metaData == null)
            {
                throw new NullPointerException();
            }
            String prefix = name + '.';
            for (MapField each : metaData.getFieldInfos())
            {
                dbColNameMap.put(each.getFieldName(), prefix + each.getColName());
                dbColNameMap.put(prefix + each.getFieldName(), prefix + each.getColName());
                fieldNameMap.put(prefix + each.getColName(), each.getFieldName());
            }
        }
        
        public String getDbColName(String fieldName)
        {
            return dbColNameMap.get(fieldName);
        }
    }
    
}
