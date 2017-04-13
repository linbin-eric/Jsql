package com.jfireframework.sql.interceptor;

import java.sql.Connection;
import com.jfireframework.baseutil.order.Order;

public interface SqlInterceptor extends Order
{
    
    public void intercept(InterceptorChain chain, Connection connection, String sql, Object... params);
}
