package com.jfireframework.sql.parse.sqlSource;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.parse.lexer.Lexer;
import com.jfireframework.sql.parse.lexer.token.Expression;
import com.jfireframework.sql.parse.lexer.token.Token;
import com.jfireframework.sql.resultsettransfer.ResultsetTransferStore;

public class StaticSqlSource extends AbstractSqlSource
{
    private final ResultsetTransferStore resultsetTransferStore;
    
    public StaticSqlSource(ResultsetTransferStore resultsetTransferStore)
    {
        this.resultsetTransferStore = resultsetTransferStore;
    }
    
    interface BuildReturnSql
    {
        String run(String methodBody, String[] paramNames, Class<?>[] paramTypes, String sql, int sn);
    }
    
    String parse(Lexer lexer, Method method, BuildReturnSql buildReturnSql)
    {
        String[] paramNames = method.getAnnotation(Sql.class).paramNames().split(",");
        Class<?>[] paramTypes = method.getParameterTypes();
        String methodBody = "";
        methodBody += "com.jfireframework.sql.SqlSession session = sessionFactory.getCurrentSession();\r\n";
        methodBody += "if(session==null){throw new java.lang.NullPointerException(\"current session 为空，请检查\");}\r\n";
        int sn = resultsetTransferStore.registerTransfer(method);
        StringCache sqlCache = new StringCache();
        List<String> params = new ArrayList<String>();
        for (Token token : lexer.getTokens())
        {
            if (token.getTokenType() == Expression.VARIABLE)
            {
                sqlCache.append('?').append(' ');
                params.add(buildParam(token.getLiterals().substring(1), paramNames, paramTypes));
            }
            else if (token.getTokenType() == Expression.CONSTANT)
            {
                sqlCache.append('?').append(' ');
                params.add(token.getLiterals().substring(1));
            }
            else
            {
                sqlCache.append(token.getLiterals()).append(' ');
            }
        }
        sqlCache.deleteLast();
        methodBody = buildReturnSql.run(methodBody, paramNames, paramTypes, sqlCache.toString(), sn);
        if (params.isEmpty())
        {
            methodBody += ");\r\n";
        }
        else
        {
            for (String param : params)
            {
                methodBody += "," + param;
            }
            methodBody += ");\r\n";
        }
        return methodBody;
    }
    
    @Override
    public String parseSingleQuery(Lexer lexer, final Method method)
    {
        BuildReturnSql buildReturnSql = new BuildReturnSql() {
            
            @Override
            public String run(String methodBody, String[] paramNames, Class<?>[] paramTypes, String sql, int sn)
            {
                methodBody += "return (" + SmcHelper.getTypeName(method.getReturnType()) + ")session.query("//
                        + "sessionFactory.getResultSetTransferStore().get(" + sn + "),"//
                        + "\"" + sql + "\"";
                return methodBody;
            }
            
        };
        return parse(lexer, method, buildReturnSql);
    }
    
    @Override
    public String parseListQuery(Lexer lexer, MetaContext metaContext, final Method method)
    {
        BuildReturnSql buildReturnSql = new BuildReturnSql() {
            
            @Override
            public String run(String methodBody, String[] paramNames, Class<?>[] paramTypes, String sql, int sn)
            {
                methodBody += "return (" + SmcHelper.getTypeName(method.getReturnType()) + ")session.queryList("//
                        + "sessionFactory.getResultSetTransferStore().get(" + sn + "),"//
                        + "\"" + sql + "\"";
                return methodBody;
            }
        };
        return parse(lexer, method, buildReturnSql);
    }
    
    @Override
    public String parsePageQuery(Lexer lexer, MetaContext metaContext, final Method method)
    {
        BuildReturnSql buildReturnSql = new BuildReturnSql() {
            
            @Override
            public String run(String methodBody, String[] paramNames, Class<?>[] paramTypes, String sql, int sn)
            {
                methodBody += "return (" + SmcHelper.getTypeName(method.getReturnType()) + ")session.queryList("//
                        + "sessionFactory.getResultSetTransferStore().get(" + sn + "),"//
                        + "\"" + sql + "\",$" + (paramTypes.length - 1);
                return methodBody;
            }
        };
        return parse(lexer, method, buildReturnSql);
    }
    
    @Override
    public String parseUpdate(Lexer lexer, MetaContext metaContext, Method method)
    {
        String[] paramNames = method.getAnnotation(Sql.class).paramNames().split(",");
        Class<?>[] paramTypes = method.getParameterTypes();
        String methodBody = "";
        methodBody += "com.jfireframework.sql.SqlSession session = sessionFactory.getCurrentSession();\r\n";
        methodBody += "if(session==null){throw new java.lang.NullPointerException(\"current session 为空，请检查\");}\r\n";
        StringCache sqlCache = new StringCache();
        List<String> params = new ArrayList<String>();
        for (Token token : lexer.getTokens())
        {
            if (token.getTokenType() == Expression.VARIABLE)
            {
                sqlCache.append('?').append(' ');
                params.add(buildParam(token.getLiterals().substring(1), paramNames, paramTypes));
            }
            else if (token.getTokenType() == Expression.CONSTANT)
            {
                sqlCache.append('?').append(' ');
                params.add(token.getLiterals().substring(1));
            }
            else
            {
                sqlCache.append(token.getLiterals()).append(' ');
            }
        }
        sqlCache.deleteLast();
        methodBody += "int updateRows = session.update(\"" + sqlCache.toString() + "\"";
        if (params.isEmpty())
        {
            methodBody += ");\r\n";
        }
        else
        {
            for (String param : params)
            {
                methodBody += "," + param;
            }
            methodBody += ");\r\n";
        }
        if (method.getReturnType() == Void.class || method.getReturnType() == void.class)
        {
            ;
        }
        else if (method.getReturnType() == int.class || method.getReturnType() == Integer.class)
        {
            methodBody += "return updateRows;\r\n";
        }
        else
        {
            throw new UnsupportedOperationException(StringUtil.format("方法:{}.{}的返回类型错误。", method.getDeclaringClass().getName(), method.getName()));
        }
        return methodBody;
    }
    
}
