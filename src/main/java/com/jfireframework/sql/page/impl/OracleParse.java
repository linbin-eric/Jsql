package com.jfireframework.sql.page.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import com.jfireframework.sql.interceptor.InterceptorChain;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.page.PageSqlCache;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;

public class OracleParse implements PageParse
{
    private String parseQuerySql(String originSql)
    {
        String querySql = PageSqlCache.getQuerySql(originSql);
        if (querySql == null)
        {
            querySql = "select * from ( select a.*,rownum rn from(" + originSql + ") a where rownum<=?) where rn>=?";
            PageSqlCache.putQuerySql(originSql, querySql);
        }
        return querySql;
    }
    
    private String parseCountSql(String originSql)
    {
        String countSql = PageSqlCache.getCountSql(originSql);
        if (countSql == null)
        {
            countSql = "select count(*) from ( " + originSql + " )";
            PageSqlCache.putCountSql(originSql, countSql);
        }
        return countSql;
    }
    
    @Override
    public void doQuery(Object[] params, Connection connection, String sql, ResultSetTransfer transfer, Page page, SqlInterceptor[] interceptors) throws Exception
    {
        PreparedStatement pstat = null;
        ResultSet resultSet = null;
        try
        {
            String querySql = parseQuerySql(sql);
            String countSql = parseCountSql(sql);
            pstat = connection.prepareStatement(querySql);
            int index = 1;
            for (Object param : params)
            {
                pstat.setObject(index++, param);
            }
            pstat.setInt(index++, page.getOffset() + page.getSize());
            pstat.setInt(index, page.getOffset());
            Object[] newParams = new Object[params.length + 2];
            System.arraycopy(params, 0, newParams, 0, params.length);
            newParams[params.length] = page.getOffset();
            newParams[params.length + 1] = page.getSize();
            if (interceptors.length != 0)
            {
                InterceptorChain chain = new InterceptorChain(interceptors);
                if (chain.intercept(connection, querySql, newParams))
                {
                    querySql = chain.getSql();
                    newParams = chain.getParams();
                }
                else
                {
                    return;
                }
            }
            resultSet = pstat.executeQuery();
            List<?> list = transfer.transferList(resultSet, querySql);
            page.setData(list);
            resultSet.close();
            pstat.close();
            pstat = connection.prepareStatement(countSql);
            index = 1;
            for (Object param : params)
            {
                pstat.setObject(index++, param);
            }
            if (interceptors.length != 0)
            {
                InterceptorChain chain = new InterceptorChain(interceptors);
                if (chain.intercept(connection, countSql, params))
                {
                    countSql = chain.getSql();
                    params = chain.getParams();
                }
                else
                {
                    return;
                }
            }
            resultSet = pstat.executeQuery();
            resultSet.next();
            page.setTotal(resultSet.getInt(1));
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
            if (pstat != null)
            {
                pstat.close();
            }
        }
    }
    
    @Override
    public void queryWithoutCount(Object[] params, Connection connection, String sql, ResultSetTransfer transfer, Page page, SqlInterceptor[] interceptors) throws Exception
    {
        PreparedStatement pstat = null;
        ResultSet resultSet = null;
        try
        {
            String querySql = parseQuerySql(sql);
            pstat = connection.prepareStatement(querySql);
            int index = 1;
            for (Object param : params)
            {
                pstat.setObject(index++, param);
            }
            pstat.setInt(index++, page.getOffset() + page.getSize());
            pstat.setInt(index, page.getOffset() + 1);
            Object[] newParams = new Object[params.length + 2];
            System.arraycopy(params, 0, newParams, 0, params.length);
            newParams[params.length] = page.getOffset() + page.getSize();
            newParams[params.length + 1] = page.getOffset() + 1;
            if (interceptors.length != 0)
            {
                InterceptorChain chain = new InterceptorChain(interceptors);
                if (chain.intercept(connection, querySql, newParams))
                {
                    querySql = chain.getSql();
                    newParams = chain.getParams();
                }
                else
                {
                    return;
                }
            }
            resultSet = pstat.executeQuery();
            List<?> list = transfer.transferList(resultSet, querySql);
            page.setData(list);
            resultSet.close();
            pstat.close();
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
            if (pstat != null)
            {
                pstat.close();
            }
        }
    }
    
}
