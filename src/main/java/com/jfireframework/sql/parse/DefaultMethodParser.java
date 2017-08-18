package com.jfireframework.sql.parse;

import java.lang.reflect.Method;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.parse.lexer.Lexer;
import com.jfireframework.sql.parse.sqlSource.DynamicSqlSource;
import com.jfireframework.sql.parse.sqlSource.SqlSource;
import com.jfireframework.sql.parse.sqlSource.StaticSqlSource;
import com.jfireframework.sql.resultsettransfer.ResultsetTransferStore;

public class DefaultMethodParser implements MethodParser
{
    private final SqlSource staticSqlSource;
    private final SqlSource dynamicSqlSource;
    
    public DefaultMethodParser(ResultsetTransferStore resultsetTransferStore)
    {
        staticSqlSource = new StaticSqlSource(resultsetTransferStore);
        dynamicSqlSource = new DynamicSqlSource(resultsetTransferStore);
    }
    
    @Override
    public String parseSingleQuery(String sql, MetaContext metaContext, Method method)
    {
        Lexer lexer = new Lexer(sql);
        lexer.parse(metaContext);
        if (lexer.isDynamic())
        {
            return dynamicSqlSource.parseSingleQuery(lexer, method);
        }
        else
        {
            return staticSqlSource.parseSingleQuery(lexer, method);
        }
    }
    
    @Override
    public String parseListQuery(String sql, MetaContext metaContext, Method method)
    {
        Lexer lexer = new Lexer(sql);
        lexer.parse(metaContext);
        if (lexer.isDynamic())
        {
            return dynamicSqlSource.parseListQuery(lexer, method);
        }
        else
        {
            return staticSqlSource.parseListQuery(lexer, method);
        }
    }
    
    @Override
    public String parsePageQuery(String sql, MetaContext metaContext, Method method)
    {
        Lexer lexer = new Lexer(sql);
        lexer.parse(metaContext);
        if (lexer.isDynamic())
        {
            return dynamicSqlSource.parsePageQuery(lexer, method);
        }
        else
        {
            return staticSqlSource.parsePageQuery(lexer, method);
        }
    }
    
    @Override
    public String parseUpdate(String sql, MetaContext metaContext, Method method)
    {
        Lexer lexer = new Lexer(sql);
        lexer.parse(metaContext);
        if (lexer.isDynamic())
        {
            return dynamicSqlSource.parseUpdate(lexer, method);
        }
        else
        {
            return staticSqlSource.parseUpdate(lexer, method);
        }
    }
}
