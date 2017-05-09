package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;

public abstract class AbstractResultsetTransfer<T> implements ResultSetTransfer<T>
{
    
    public AbstractResultsetTransfer(Class<?> ckass)
    {
    }
    
    @Override
    public T transfer(ResultSet resultSet, String sql) throws Exception
    {
        if (resultSet.next())
        {
            T result = valueOf(resultSet, sql);
            if (resultSet.next())
            {
                throw new IllegalArgumentException(StringUtil.format("存在2行数据，不符合返回值要求。"));
            }
            else
            {
                return result;
            }
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public List<T> transferList(ResultSet resultSet, String sql) throws Exception
    {
        List<T> list = new LinkedList<T>();
        while (resultSet.next())
        {
            list.add(valueOf(resultSet, sql));
        }
        return list;
    }
    
    protected abstract T valueOf(ResultSet resultSet, String sql) throws Exception;
}
