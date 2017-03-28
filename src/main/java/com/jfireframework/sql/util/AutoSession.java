package com.jfireframework.sql.util;

import javax.annotation.Resource;
import com.jfireframework.baseutil.simplelog.ConsoleLogFactory;
import com.jfireframework.baseutil.simplelog.Logger;
import com.jfireframework.jfire.tx.RessourceManager;
import com.jfireframework.sql.session.SessionFactory;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.session.impl.SqlSessionImpl;

@Resource
public class AutoSession implements RessourceManager
{
    @Resource
    private SessionFactory sessionFactory;
    private static Logger  logger = ConsoleLogFactory.getLogger();
    
    @Override
    public void close()
    {
        logger.trace("关闭当前session{}", sessionFactory.getCurrentSession());
        SqlSession session = sessionFactory.getCurrentSession();
        if (session != null)
        {
            ((SqlSessionImpl) session).autoClose();
        }
    }
    
    @Override
    public void open()
    {
        SqlSessionImpl sessionImpl = (SqlSessionImpl) sessionFactory.getOrCreateCurrentSession();
        sessionImpl.autoOpen();
    }
    
}
