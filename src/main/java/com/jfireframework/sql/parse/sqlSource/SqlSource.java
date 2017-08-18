package com.jfireframework.sql.parse.sqlSource;

import java.lang.reflect.Method;
import com.jfireframework.sql.parse.lexer.Lexer;

public interface SqlSource
{
    String parseSingleQuery(Lexer lexer, Method method);
    
    String parseListQuery(Lexer lexer, Method method);
    
    String parsePageQuery(Lexer lexer, Method method);
    
    String parseUpdate(Lexer lexer, Method method);
}
