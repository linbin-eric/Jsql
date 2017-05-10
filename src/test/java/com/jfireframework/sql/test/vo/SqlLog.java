package com.jfireframework.sql.test.vo;

import java.sql.Connection;
import com.jfireframework.sql.interceptor.InterceptorChain;
import com.jfireframework.sql.interceptor.SqlInterceptor;

public class SqlLog implements SqlInterceptor
{
    
    @Override
    public int getOrder()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public void intercept(InterceptorChain chain, Connection connection, String sql, Object... params)
    {
        System.out.println(sql);
        chain.intercept(connection, sql, params);
    }
    
}
