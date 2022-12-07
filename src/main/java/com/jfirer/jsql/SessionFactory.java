package com.jfirer.jsql;

import com.jfirer.jsql.session.SqlSession;

public interface SessionFactory
{
    /**
     * 打开一个SqlSession
     *
     * @return
     */
    SqlSession openSession();
}
