package com.jfireframework.sql.mapper;

import java.lang.reflect.Method;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.compiler.JavaStringCompiler;
import com.jfireframework.baseutil.smc.model.CompilerModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.parse.DefaultMethodParser;
import com.jfireframework.sql.parse.MethodParser;
import com.jfireframework.sql.transfer.resultset.ResultsetTransferStore;

public class MapperBuilder
{
    private final MetaContext   metaContext;
    private final MethodParser  methodParser;
    private static final Logger logger = LoggerFactory.getLogger(MapperBuilder.class);
    
    public MapperBuilder(MetaContext metaContext, ResultsetTransferStore resultsetTransferStore)
    {
        this.metaContext = metaContext;
        methodParser = new DefaultMethodParser(resultsetTransferStore);
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
                throw new RuntimeException(StringUtil.format("接口存在错误，请检查{}.{}.Sql语句:{}", method.getDeclaringClass().getName(), method.getName(), method.getAnnotation(Sql.class).sql()), e);
            }
        }
        
    }
    
    private boolean detectIsList(Method method)
    {
        return List.class.isAssignableFrom(method.getReturnType()) ? true : false;
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
        boolean isList = detectIsList(method);
        String methodBody = null;
        if (isList)
        {
            boolean isPage = detectIsPage(method);
            if (isPage)
            {
                methodBody = methodParser.parsePageQuery(sql, metaContext, method);
            }
            else
            {
                methodBody = methodParser.parseListQuery(sql, metaContext, method);
            }
        }
        else
        {
            methodBody = methodParser.parseSingleQuery(sql, metaContext, method);
        }
        MethodModel methodModel = new MethodModel(method);
        methodModel.setBody(methodBody);
        compilerModel.putMethod(method, methodModel);
    }
    
    private void createUpdateMethod(CompilerModel compilerModel, Method method, String sql, String[] paramNames) throws Exception
    {
        String methodBody = methodParser.parseUpdate(sql, metaContext, method);
        MethodModel methodModel = new MethodModel(method);
        methodModel.setBody(methodBody);
        compilerModel.putMethod(method, methodModel);
    }
    
}
