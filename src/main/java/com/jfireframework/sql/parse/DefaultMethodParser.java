package com.jfireframework.sql.parse;

import java.lang.reflect.Method;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.parse.lexer.Lexer;
import com.jfireframework.sql.parse.sqlSource.SqlSource;
import com.jfireframework.sql.parse.sqlSource.StaticSqlSource;
import com.jfireframework.sql.resultsettransfer.ResultsetTransferStore;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class DefaultMethodParser implements MethodParser
{
    private final ResultsetTransferStore resultsetTransferStore;
    private final JdbcTypeDictionary     jdbcTypeDictionary;
    private SqlSource                    staticSqlSource;
    private SqlSource                    dynamicSqlSource;
    
    public DefaultMethodParser(ResultsetTransferStore resultsetTransferStore, JdbcTypeDictionary jdbcTypeDictionary)
    {
        this.resultsetTransferStore = resultsetTransferStore;
        this.jdbcTypeDictionary = jdbcTypeDictionary;
        staticSqlSource = new StaticSqlSource(resultsetTransferStore);
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
