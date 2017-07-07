package com.jfireframework.sql.mapfield.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import com.jfireframework.baseutil.reflect.ReflectUtil;

public class EnumOrdinalOperator extends AbstractFieldOperator
{
    Enum<?>[] allEnumInstances;
    
    @Override
    @SuppressWarnings({ "unchecked" })
    public void initialize(Field field)
    {
        Map<String, ? extends Enum<?>> instances = ReflectUtil.getAllEnumInstances((Class<? extends Enum<?>>) field.getType());
        allEnumInstances = new Enum[instances.size()];
        for (Enum<?> each : instances.values())
        {
            allEnumInstances[each.ordinal()] = each;
        }
    }
    
    @Override
    public void setEntityValue(Object entity, Field field, String dbColName, long offset, ResultSet resultSet) throws SQLException
    {
        int value = resultSet.getInt(dbColName);
        Enum<?> result = allEnumInstances[value];
        unsafe.putObject(entity, offset, result);
    }
    
    @Override
    public Object fieldValue(Object entity, Field field, long offset)
    {
        return unsafe.getObject(entity, offset);
    }
    
}
