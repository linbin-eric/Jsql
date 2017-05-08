package com.jfireframework.sql.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.interceptor.InterceptorChain;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;

public class ExecSqlTemplate
{
    
    public static final int query     = 1;
    public static final int queryList = 2;
    public static final int update    = 3;
    public static final int page      = 4;
    
    public static int[] batchInsert(SqlInterceptor[] interceptors, Connection connection, String sql, Object... paramArrays)
    {
        PreparedStatement pstat = null;
        ResultSet resultSet = null;
        try
        {
            if (interceptors.length != 0)
            {
                InterceptorChain chain = new InterceptorChain(interceptors);
                if (chain.intercept(connection, sql,  paramArrays))
                {
                    sql = chain.getSql();
                    paramArrays =  chain.getParams();
                }
                else
                {
                    return chain.getResult();
                }
            }
            pstat = connection.prepareStatement(sql);
            for (Object each : paramArrays)
            {
                int index = 1;
                for (Object eachParam : (Object[]) each)
                {
                    pstat.setObject(index++, eachParam);
                }
                pstat.addBatch();
            }
            return pstat.executeBatch();
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
        finally
        {
            try
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
            catch (SQLException e)
            {
                throw new JustThrowException(e);
            }
        }
    }
    
    public static Object insert(IdType idType, String[] pkName, SqlInterceptor[] interceptors, Connection connection, String sql, Object... params)
    {
        PreparedStatement pstat = null;
        ResultSet resultSet = null;
        try
        {
            if (interceptors.length != 0)
            {
                InterceptorChain chain = new InterceptorChain(interceptors);
                if (chain.intercept(connection, sql, params))
                {
                    sql = chain.getSql();
                    params = chain.getParams();
                }
                else
                {
                    return chain.getResult();
                }
            }
            pstat = connection.prepareStatement(sql, pkName);
            int index = 1;
            for (Object each : params)
            {
                pstat.setObject(index++, each);
            }
            pstat.executeUpdate();
            resultSet = pstat.getGeneratedKeys();
            if (resultSet.next())
            {
                switch (idType)
                {
                    case INT:
                        return resultSet.getInt(1);
                    case LONG:
                        return resultSet.getLong(1);
                    case STRING:
                        return resultSet.getString(1);
                    default:
                        throw new UnsupportedOperationException();
                }
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
        finally
        {
            try
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
            catch (SQLException e)
            {
                throw new JustThrowException(e);
            }
        }
    }
    
    public static Object exec(int mode, SqlInterceptor[] interceptors, PageParse parse, Page pageStore, ResultSetTransfer<?> transfer, Connection connection, String sql, Object... params)
    {
        PreparedStatement pstat = null;
        ResultSet resultSet = null;
        try
        {
            if (interceptors.length != 0)
            {
                InterceptorChain chain = new InterceptorChain(interceptors);
                if (chain.intercept(connection, sql, params))
                {
                    sql = chain.getSql();
                    params = chain.getParams();
                }
                else
                {
                    return chain.getResult();
                }
            }
            if (mode == page)
            {
                parse.doQuery(params, connection, sql, transfer, pageStore);
                return pageStore.getData();
            }
            else
            {
                pstat = connection.prepareStatement(sql);
                int index = 1;
                for (Object each : params)
                {
                    pstat.setObject(index++, each);
                }
                switch (mode)
                {
                    case query:
                    {
                        resultSet = pstat.executeQuery();
                        return transfer.transfer(resultSet, sql);
                    }
                    case queryList:
                    {
                        resultSet = pstat.executeQuery();
                        return transfer.transferList(resultSet, sql);
                    }
                    case update:
                    {
                        return pstat.executeUpdate();
                    }
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
        finally
        {
            try
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
            catch (SQLException e)
            {
                throw new JustThrowException(e);
            }
        }
        
    }
    
}
