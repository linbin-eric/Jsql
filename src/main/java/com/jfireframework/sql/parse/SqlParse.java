package com.jfireframework.sql.parse;

import java.lang.reflect.Method;
import com.jfireframework.sql.metadata.MetaContext;

public interface SqlParse
{
    /**
     * 将sql语句解析成实际可运行的源代码进行编译
     * 
     * @param sql
     * @return
     */
    String parse(String sql, MetaContext metaContext, Method method);
}
