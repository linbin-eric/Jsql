package com.jfirer.jsql.transfer.impl;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.transfer.CustomTransfer;
import com.jfirer.jsql.transfer.ResultSetTransfer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.Calendar;

public class BeanTransfer implements ResultSetTransfer
{
    record ColumnTransfer(ResultSetTransfer transfer, ValueAccessor accessor) {}

    private          Class<?>         ckass;
    private volatile ColumnTransfer[] columnTransfers;

    @Override
    public Object transfer(ResultSet resultSet) throws SQLException
    {
        if (columnTransfers == null)
        {
            synchronized (ckass)
            {
                if (columnTransfers == null)
                {
                    ResultSetMetaData metaData        = resultSet.getMetaData();
                    int               columnCount     = metaData.getColumnCount();
                    ColumnTransfer[]  transfers       = new ColumnTransfer[columnCount];
                    TableEntityInfo   tableEntityInfo = TableEntityInfo.parse(ckass);
                    for (int i = 0; i < columnCount; i++)
                    {
                        ColumnTransfer columnTransfer;
                        String         columnName = metaData.getColumnName(i + 1);
                        Field          field      = tableEntityInfo.getColumnInfoByColumnNameIgnoreCase(columnName).getField();
                        Class          fieldType  = field.getType().isPrimitive() ? ReflectUtil.wrapPrimitive(field.getType()) : field.getType();
                        if (field.isAnnotationPresent(CustomTransfer.class))
                        {
                            try
                            {
                                ColumnIndexHolder columnIndexHolder = field.getAnnotation(CustomTransfer.class).value().getDeclaredConstructor(int.class).newInstance(i + 1);
                                columnIndexHolder.awareType(field.getType());
                                columnTransfer = new ColumnTransfer(columnIndexHolder, new ValueAccessor(field));
                            }
                            catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                   NoSuchMethodException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }
                        else if (fieldType == Boolean.class)
                        {
                            columnTransfer = new ColumnTransfer(new BooleanTransfer(i + 1), new ValueAccessor(field));
                        }
                        else if (fieldType == Double.class)
                        {
                            columnTransfer = new ColumnTransfer(new DoubleTransfer(i + 1), new ValueAccessor(field));
                        }
                        else if (fieldType == Float.class)
                        {
                            columnTransfer = new ColumnTransfer(new FloatTransfer(i + 1), new ValueAccessor(field));
                        }
                        else if (fieldType == Integer.class)
                        {
                            columnTransfer = new ColumnTransfer(new IntegerTransfer(i + 1), new ValueAccessor(field));
                        }
                        else if (fieldType == Long.class)
                        {
                            columnTransfer = new ColumnTransfer(new LongTransfer(i + 1), new ValueAccessor(field));
                        }
                        else if (fieldType == Short.class)
                        {
                            columnTransfer = new ColumnTransfer(new ShortTransfer(i + 1), new ValueAccessor(field));
                        }
                        else if (fieldType == Date.class)
                        {
                            columnTransfer = new ColumnTransfer(new SqlDateTransfer(i + 1), new ValueAccessor(field));
                        }
                        else if (fieldType == java.util.Date.class)
                        {
                            columnTransfer = new ColumnTransfer(new UtilDateTransfer(i + 1), new ValueAccessor(field));
                        }
                        else if (fieldType == String.class)
                        {
                            columnTransfer = new ColumnTransfer(new StringTransfer(i + 1), new ValueAccessor(field));
                        }
                        else if (fieldType == Timestamp.class)
                        {
                            columnTransfer = new ColumnTransfer(new TimeStampTransfer(i + 1), new ValueAccessor(field));
                        }
                        else if (fieldType == Time.class)
                        {
                            columnTransfer = new ColumnTransfer(new TimeTransfer(i + 1), new ValueAccessor(field));
                        }
                        else if (Enum.class.isAssignableFrom(fieldType))
                        {
                            columnTransfer = new ColumnTransfer(new EnumNameTransfer(i + 1).awareType(fieldType), new ValueAccessor(field));
                        }
                        else if (fieldType == Calendar.class)
                        {
                            columnTransfer = new ColumnTransfer(new CalendarTransfer(i + 1), new ValueAccessor(field));
                        }
                        else if (fieldType == byte[].class)
                        {
                            columnTransfer = new ColumnTransfer(new ByteArrayTransfer(i + 1), new ValueAccessor(field));
                        }
                        else if (fieldType == Clob.class)
                        {
                            columnTransfer = new ColumnTransfer(new ClobTransfer(i + 1), new ValueAccessor(field));
                        }
                        else
                        {
                            throw new IllegalArgumentException();
                        }
                        transfers[i] = columnTransfer;
                    }
                    this.columnTransfers = transfers;
                }
            }
        }
        try
        {
            Object entity = ckass.newInstance();
            for (ColumnTransfer each : columnTransfers)
            {
                Object value = each.transfer.transfer(resultSet);
                if (value != null)
                {
                    each.accessor.setObject(entity, value);
                }
            }
            return entity;
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    @Override
    public ResultSetTransfer awareType(Class type)
    {
        this.ckass = type;
        return this;
    }
}
