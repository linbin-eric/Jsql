package com.jfireframework.sql.transfer.resultset.impl;

import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class EnumOrdinalTransfer implements ResultSetTransfer
{
    private Enum<?>[] instances;

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        int ordinal = resultSet.getInt(1);
        if ( resultSet.wasNull() )
        {
            return null;
        }
        return instances[ordinal];
    }

    @SuppressWarnings("unchecked")
    @Override
    public ResultSetTransfer initialize(Class<?> type)
    {
        Map<String, ? extends Enum<?>> allEnumInstances = ReflectUtil.getAllEnumInstances((Class<? extends Enum<?>>) type);
        Enum<?>[] instances = new Enum[allEnumInstances.size()];
        for (Enum<?> each : allEnumInstances.values())
        {
            instances[each.ordinal()] = each;
        }
        this.instances = instances;
        return this;
    }

}
