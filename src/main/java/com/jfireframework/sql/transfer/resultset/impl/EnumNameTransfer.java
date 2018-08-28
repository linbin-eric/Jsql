package com.jfireframework.sql.transfer.resultset.impl;

import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EnumNameTransfer implements ResultSetTransfer
{
    @SuppressWarnings("rawtypes")
    private Class<? extends Enum> type;

    @SuppressWarnings("unchecked")
    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        String enumName = resultSet.getString(1);
        if ( enumName == null )
        {
            return null;
        }
        return Enum.valueOf(type, enumName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ResultSetTransfer initialize(Class<?> type)
    {
        this.type = (Class<? extends Enum<?>>) type;
        return this;
    }

}
