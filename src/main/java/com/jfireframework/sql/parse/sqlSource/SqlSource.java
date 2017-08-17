package com.jfireframework.sql.parse.sqlSource;

import java.lang.reflect.Method;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.parse.lexer.Lexer;

public interface SqlSource
{
    String parseSingleQuery(Lexer lexer, Method method);
    
    String parseListQuery(Lexer lexer, MetaContext metaContext, Method method);
    
    String parsePageQuery(Lexer lexer, MetaContext metaContext, Method method);
    
    String parseUpdate(Lexer lexer, MetaContext metaContext, Method method);
}
