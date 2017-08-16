package com.jfireframework.sql.parse;

import java.lang.reflect.Method;
import com.jfireframework.sql.metadata.MetaContext;

public class AbstractSqlParser implements SqlParse
{
    
    @Override
    public String parse(String sql, MetaContext metaContext, Method method)
    {
        sql = transform(sql);
        return null;
    }
    
    /**
     * 将sql语句中的类名和属性名转化为表名和表字段名
     * 
     * @param sql
     * @return
     */
    String transform(String sql)
    {
        
    }
}
