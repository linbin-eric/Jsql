package com.jfirer.jsql.transfer.column.impl;


import com.jfirer.baseutil.reflect.ReflectUtil;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class EnumOrdinalTransfer extends AbstractColumnTransfer
{
    private Enum<?>[] allEnumInstances;

    @Override
    @SuppressWarnings({"unchecked"})
    public void initialize(Field field, String columnName)
    {
        super.initialize(field, columnName);
        Map<String, ? extends Enum<?>> instances = ReflectUtil.getAllEnumInstances((Class<? extends Enum<?>>) field.getType());
        allEnumInstances = new Enum[instances.size()];
        for (Enum<?> each : instances.values())
        {
            allEnumInstances[each.ordinal()] = each;
        }
    }

    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException, IllegalArgumentException, IllegalAccessException
    {
        int value = resultSet.getInt(columnName);
        if ( resultSet.wasNull() == false )
        {
            field.set(entity, allEnumInstances[value]);
        }
    }

}
