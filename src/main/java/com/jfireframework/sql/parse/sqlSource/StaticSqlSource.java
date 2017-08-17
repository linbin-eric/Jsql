package com.jfireframework.sql.parse.sqlSource;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.sql.parse.lexer.Lexer;
import com.jfireframework.sql.parse.lexer.token.Expression;
import com.jfireframework.sql.parse.lexer.token.Token;
import com.jfireframework.sql.resultsettransfer.ResultsetTransferStore;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class StaticSqlSource extends AbstractSqlSource
{
    
    @Override
    public String parseSingleQuery(Lexer lexer, String[] paramNames, Class<?>[] paramTypes, Method method, ResultsetTransferStore resultsetTransferStore, JdbcTypeDictionary jdbcTypeDictionary)
    {
        String methodBody = "";
        methodBody += "com.jfireframework.sql.SqlSession session = sessionFactory.getCurrentSession();\r\n";
        methodBody += "if(session==null){throw new java.lang.NullPointerException(\"current session 为空，请检查\");}\r\n";
        Class<?> returnType = method.getReturnType();
        int sn = resultsetTransferStore.registerTransfer(method, jdbcTypeDictionary);
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
        methodBody += "return (" + SmcHelper.getTypeName(returnType) + ")session.query("//
                + "sessionFactory.getResultSetTransferStore().get(" + sn + "),"//
                + "\"" + sqlCache.toString() + "\"";
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
    
}
