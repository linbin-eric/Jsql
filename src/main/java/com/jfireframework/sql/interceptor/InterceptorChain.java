package com.jfireframework.sql.interceptor;

import java.sql.Connection;

public class InterceptorChain
{
    private final SqlInterceptor[] interceptors;
    private int                    index = 0;
    private String                 sql;
    private Object[]               params;
    private Object                 result;
    
    public InterceptorChain(SqlInterceptor[] interceptors)
    {
        this.interceptors = interceptors;
    }
    
    public boolean intercept(Connection connection, String sql, Object... params)
    {
        doChain(connection, sql, params);
        return index == interceptors.length ? true : false;
    }
    
    protected void doChain(Connection connection, String sql, Object... params)
    {
        SqlInterceptor now = interceptors[index];
        this.sql = sql;
        this.params = params;
        index += 1;
        now.intercept(this, connection, sql, params);
    }
    
    public String getSql()
    {
        return sql;
    }
    
    public Object[] getParams()
    {
        return params;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getResult()
    {
        return (T) result;
    }
    
    public void setResult(Object result)
    {
        this.result = result;
    }
    
}
