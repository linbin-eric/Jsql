package com.jfireframework.sql.mapfield.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import com.jfireframework.baseutil.reflect.ReflectUtil;

public class EnumNameFetcher extends AbstractFieldOperator
{
    Map<String, ? extends Enum<?>> allEnumInstances;
    
    @Override
    @SuppressWarnings({ "unchecked" })
    public void initialize(Field field)
    {
        allEnumInstances = ReflectUtil.getAllEnumInstances((Class<? extends Enum<?>>) field.getType());
    }
    
    @Override
    public void setEntityValue(Object entity, Field field, String dbColName, long offset, ResultSet resultSet) throws SQLException
    {
        String value = resultSet.getString(dbColName);
        Enum<?> result = allEnumInstances.get(value);
        unsafe.putObject(entity, offset, result);
    }
    
    @Override
    public Object fieldValue(Object entity, Field field, long offset)
    {
        return unsafe.getObject(entity, offset);
    }
    
}
