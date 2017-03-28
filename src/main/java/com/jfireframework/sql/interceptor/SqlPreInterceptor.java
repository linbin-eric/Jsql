package com.jfireframework.sql.interceptor;

public interface SqlPreInterceptor
{
    public String preIntercept(String sql, Object... params);
}
