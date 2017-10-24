package com.jfireframework.sql.resultsettransfer.impl;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;

public abstract class AbstractResultsetTransfer implements ResultSetTransfer
{
    
    @Override
    public Object transfer(ResultSet resultSet) throws Exception
    {
        if (resultSet.next())
        {
            Object result = valueOf(resultSet);
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
    public List<Object> transferList(ResultSet resultSet) throws Exception
    {
        List<Object> list = new LinkedList<Object>();
        while (resultSet.next())
        {
            list.add(valueOf(resultSet));
        }
        return list;
    }
    
    protected abstract Object valueOf(ResultSet resultSet) throws Exception;
}
