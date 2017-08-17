package com.jfireframework.sql.parse;

import java.lang.reflect.Method;
import com.jfireframework.sql.metadata.MetaContext;

public interface MethodParser
{
    /**
     * 将sql查询解析为查询单个的动态编译代码
     * 
     * @param sql
     * @param metaContext
     * @param method
     * @return
     */
    String parseSingleQuery(String sql, MetaContext metaContext, Method method);
    
    /**
     * 将sql查询解析为查询List结果集的动态编译代码
     * 
     * @param sql
     * @param metaContext
     * @param method
     * @return
     */
    String parseListQuery(String sql, MetaContext metaContext, Method method);
    
    /**
     * 将sql查询解析为分页查询结果集的动态编译代码
     * 
     * @param sql
     * @param metaContext
     * @param method
     * @return
     */
    String parsePageQuery(String sql, MetaContext metaContext, Method method);
    
    /**
     * 将sql更新解析为动态编译代码
     * 
     * @param sql
     * @param metaContext
     * @param method
     * @return
     */
    String parseUpdate(String sql, MetaContext metaContext, Method method);
}
