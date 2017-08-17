package com.jfireframework.sql.parse.sqlSource;

import java.lang.reflect.Method;
import com.jfireframework.sql.parse.lexer.Lexer;
import com.jfireframework.sql.resultsettransfer.ResultsetTransferStore;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public interface SqlSource
{
    String parseSingleQuery(Lexer lexer, String[] paramNames, Class<?>[] paramTypes, Method method, ResultsetTransferStore resultsetTransferStore, JdbcTypeDictionary jdbcTypeDictionary);
}
