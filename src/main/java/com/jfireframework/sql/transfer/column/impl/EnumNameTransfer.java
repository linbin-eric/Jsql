package com.jfireframework.sql.transfer.column.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EnumNameTransfer extends AbstractColumnTransfer
{
    @SuppressWarnings("rawtypes")
    private Class<? extends Enum> type;

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void initialize(Field field, String columnName)
    {
        super.initialize(field, columnName);
        type = (Class<? extends Enum>) field.getType();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException, IllegalArgumentException, IllegalAccessException
    {
        String value = resultSet.getString(columnName);
        if ( resultSet.wasNull() == false )
        {
            field.set(entity, Enum.valueOf(type, value));
        }
    }

}
