package com.jfireframework.sql;

import com.jfireframework.sql.session.SqlSession;

public interface SessionFactory
{
    
    /**
     * 打开一个SqlSession
     * 
     * @return
     */
    SqlSession openSession();
    
}
