package com.jfireframework.sql.transfer.column.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import com.jfireframework.baseutil.reflect.ReflectUtil;

public class EnumNameOperator extends AbstractColumnTransfer
{
    Map<String, ? extends Enum<?>> allEnumInstances;
    
    @Override
    @SuppressWarnings({ "unchecked" })
    public void initialize(Field field)
    {
        offset = unsafe.objectFieldOffset(field);
        allEnumInstances = ReflectUtil.getAllEnumInstances((Class<? extends Enum<?>>) field.getType());
    }
    
    @Override
    public void setEntityValue(Object entity, String dbColName, ResultSet resultSet) throws SQLException
    {
        String value = resultSet.getString(dbColName);
        Enum<?> result = allEnumInstances.get(value);
        unsafe.putObject(entity, offset, result);
    }
    
    
}
