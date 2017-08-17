package com.jfireframework.sql.parse;

import java.lang.reflect.Method;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.parse.lexer.Lexer;

public class AbstractSqlParser implements SqlParse
{
    
    @Override
    public String parseSingleQuery(String sql, MetaContext metaContext, Method method)
    {
        Lexer lexer = new Lexer(sql);
        lexer.parse(metaContext);
        if (lexer.isDynamic())
        {
            
        }
        else
        {
            String[] paramNames = method.getAnnotation(Sql.class).paramNames().split(",");
            Class<?>[] paramTypes = method.getParameterTypes();
            
        }
    }
    
    @Override
    public String parseListQuery(String sql, MetaContext metaContext, Method method)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String parsePageQuery(String sql, MetaContext metaContext, Method method)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String parseUpdate(String sql, MetaContext metaContext, Method method)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
