package com.jfireframework.sql.page.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.page.PageSqlCache;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;

/**
 * 基于sql92语法,使用limit的方式进行分页
 * 
 * @author linbin
 *
 */
public class StandardParse implements PageParse
{
    private String parseQuerySql(String originSql)
    {
        String querySql = PageSqlCache.getQuerySql(originSql);
        if (querySql == null)
        {
            querySql = originSql + " limit ?,?";
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
    public void doQuery(Object[] params, Connection connection, String sql, ResultSetTransfer transfer, Page page) throws Exception
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
            pstat.setInt(index++, page.getOffset());
            pstat.setInt(index, page.getSize());
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
    public void queryWithoutCount(Object[] params, Connection connection, String sql, ResultSetTransfer transfer, Page page) throws Exception
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
            pstat.setInt(index++, page.getOffset());
            pstat.setInt(index, page.getSize());
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
