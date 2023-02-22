package com.jfirer.jsql.transfer.impl;

import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.transfer.CustomTransfer;
import com.jfirer.jsql.transfer.ResultSetTransfer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

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
                    ResultSetMetaData   metaData       = resultSet.getMetaData();
                    int                 columnCount    = metaData.getColumnCount();
                    Set<ColumnTransfer> transfers      = new HashSet<>();
                    TableEntityInfo     returnTypeInfo = TableEntityInfo.parse(ckass);
                    /**
                     * 1. 如果tableName能够和returnType匹配上，则用数据库字段名查找类名对应的映射。
                     * 2. 如果tableName不能和returnType匹配上，则使用label名称查找类名对应的映射。
                     */
                    for (int i = 0; i < columnCount; i++)
                    {
                        int            columnIndex = i + 1;
                        ColumnTransfer columnTransfer;
                        String         tableName   = metaData.getTableName(columnIndex);
                        String         columnName  = metaData.getColumnName(columnIndex);
                        String         columnLabel = metaData.getColumnLabel(columnIndex);
                        Field          field;
                        if (tableName.equalsIgnoreCase(returnTypeInfo.getTableName()))
                        {
                            if (returnTypeInfo.getColumnInfoByColumnNameIgnoreCase(columnName) == null)
                            {
                                continue;
                            }
                            else
                            {
                                field = returnTypeInfo.getColumnInfoByColumnNameIgnoreCase(columnName).field();
                            }
                        }
                        else
                        {
                            if (returnTypeInfo.getColumnInfoByColumnNameIgnoreCase(columnLabel) == null)
                            {
                                continue;
                            }
                            else
                            {
                                field = returnTypeInfo.getColumnInfoByColumnNameIgnoreCase(columnLabel).field();
                            }
                        }
                        Class fieldType = field.getType().isPrimitive() ? ReflectUtil.wrapPrimitive(field.getType()) : field.getType();
                        if (AnnotationContext.isAnnotationPresent(CustomTransfer.class, field))
                        {
                            try
                            {
                                ColumnIndexHolder columnIndexHolder = AnnotationContext.getAnnotation(CustomTransfer.class, field).value().getDeclaredConstructor(int.class).newInstance(columnIndex);
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
                            columnTransfer = new ColumnTransfer(new BooleanTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else if (fieldType == Double.class)
                        {
                            columnTransfer = new ColumnTransfer(new DoubleTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else if (fieldType == Float.class)
                        {
                            columnTransfer = new ColumnTransfer(new FloatTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else if (fieldType == Integer.class)
                        {
                            columnTransfer = new ColumnTransfer(new IntegerTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else if (fieldType == Long.class)
                        {
                            columnTransfer = new ColumnTransfer(new LongTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else if (fieldType == Short.class)
                        {
                            columnTransfer = new ColumnTransfer(new ShortTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else if (fieldType == Date.class)
                        {
                            columnTransfer = new ColumnTransfer(new SqlDateTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else if (fieldType == java.util.Date.class)
                        {
                            columnTransfer = new ColumnTransfer(new UtilDateTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else if (fieldType == String.class)
                        {
                            columnTransfer = new ColumnTransfer(new StringTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else if (fieldType == Timestamp.class)
                        {
                            columnTransfer = new ColumnTransfer(new TimeStampTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else if (fieldType == Time.class)
                        {
                            columnTransfer = new ColumnTransfer(new TimeTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else if (Enum.class.isAssignableFrom(fieldType))
                        {
                            columnTransfer = new ColumnTransfer(new EnumNameTransfer(columnIndex).awareType(fieldType), new ValueAccessor(field));
                        }
                        else if (fieldType == Calendar.class)
                        {
                            columnTransfer = new ColumnTransfer(new CalendarTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else if (fieldType == byte[].class)
                        {
                            columnTransfer = new ColumnTransfer(new ByteArrayTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else if (fieldType == Clob.class)
                        {
                            columnTransfer = new ColumnTransfer(new ClobTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else if (fieldType.isEnum())
                        {
                            columnTransfer = new ColumnTransfer(new EnumNameTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else if (fieldType == BigDecimal.class)
                        {
                            columnTransfer = new ColumnTransfer(new BigDecimalTransfer(columnIndex), new ValueAccessor(field));
                        }
                        else
                        {
                            throw new IllegalArgumentException();
                        }
                        if (transfers.add(columnTransfer) == false)
                        {
                            throw new IllegalArgumentException("在一个sql语句中出现重复名称字段，重复字段为:" + columnTransfer.accessor.getField().toString());
                        }
                    }
                    this.columnTransfers = transfers.toArray(ColumnTransfer[]::new);
                }
            }
        }
        try
        {
            Object entity = ckass.getConstructor().newInstance();
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
