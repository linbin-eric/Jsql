package com.jfirer.jsql.transfer.impl;

import com.jfirer.baseutil.STR;
import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.transfer.CustomTransfer;
import com.jfirer.jsql.transfer.ResultSetTransfer;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BeanTransfer implements ResultSetTransfer
{
    private          Class<?>       ckass;
    private          Constructor<?> constructor;
    private volatile Entry[]        entries;

    @SneakyThrows
    @Override
    public Object transfer(ResultSet resultSet, int ignored)
    {
        if (entries == null)
        {
            synchronized (this)
            {
                if (entries == null)
                {
                    ResultSetMetaData metaData       = resultSet.getMetaData();
                    int               columnCount    = metaData.getColumnCount();
                    List<Entry>       list           = new ArrayList<>();
                    TableEntityInfo   returnTypeInfo = TableEntityInfo.parse(ckass);
                    for (int i = 0; i < columnCount; i++)
                    {
                        int                        columnIndex = i + 1;
                        String                     tableName   = metaData.getTableName(columnIndex);
                        String                     columnName  = metaData.getColumnName(columnIndex);
                        String                     fullname    = StringUtil.isNotBlank(tableName) ? (tableName + '.' + columnName) : columnName;
                        TableEntityInfo.ColumnInfo columnInfo  = returnTypeInfo.findColumnInfoByFullname(fullname);
                        if (columnInfo == null)
                        {
                            continue;
                        }
                        int classId = ReflectUtil.getClassId(columnInfo.field().getType());
                        if (columnInfo.field().isAnnotationPresent(CustomTransfer.class))
                        {
                            if (ReflectUtil.isPrimitive(columnInfo.field().getType()))
                            {
                                throw new IllegalArgumentException(STR.format("字段:{}的类型为基础类型，要使用包装类型才能使用注解CustomTransfer", columnInfo.field()));
                            }
                            Class<? extends ResultSetTransfer> value             = columnInfo.field().getAnnotation(CustomTransfer.class).value();
                            ResultSetTransfer                  resultSetTransfer = value.getConstructor().newInstance();
                            resultSetTransfer.awareType(columnInfo.field().getType());
                            list.add(new Entry().setAccessor(columnInfo.accessor())//
                                                .setCkazz(columnInfo.field().getType())//
                                                .setClassId(classId)//
                                                .setColumnIndex(columnIndex)//
                                                .setTransfer(resultSetTransfer));
                        }
                        else
                        {
                            list.add(new Entry().setAccessor(columnInfo.accessor())//
                                                .setClassId(classId)//
                                                .setColumnIndex(columnIndex)//
                                                .setCkazz(columnInfo.field().getType())//
                            );
                        }
                    }
                    entries = list.toArray(new Entry[0]);
                }
            }
        }
        Object result = constructor.newInstance();
        for (Entry entry : entries)
        {
            entry.fetchSqlValue(result, resultSet);
        }
        return result;
    }

    @SneakyThrows
    @Override
    public void awareType(Class type)
    {
        this.ckass  = type;
        constructor = ckass.getConstructor();
    }

    @Data
    @Accessors(chain = true)
    static class Entry
    {
        protected int               columnIndex;
        protected int               classId;
        protected ValueAccessor     accessor;
        protected ResultSetTransfer transfer;
        protected Class             ckazz;

        @SneakyThrows
        public void fetchSqlValue(Object result, ResultSet resultSet)
        {
            if (transfer != null)
            {
                Object value = transfer.transfer(resultSet, columnIndex);
                accessor.setReference(result, value);
                return;
            }
            switch (classId)
            {
                case ReflectUtil.PRIMITIVE_INT ->
                {
                    int i = resultSet.getInt(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.set(result, i);
                    }
                }
                case ReflectUtil.PRIMITIVE_BOOL ->
                {
                    boolean b = resultSet.getBoolean(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.set(result, b);
                    }
                }
                case ReflectUtil.PRIMITIVE_FLOAT ->
                {
                    float f = resultSet.getFloat(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.set(result, f);
                    }
                }
                case ReflectUtil.PRIMITIVE_DOUBLE ->
                {
                    double d = resultSet.getDouble(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.set(result, d);
                    }
                }
                case ReflectUtil.PRIMITIVE_LONG ->
                {
                    long l = resultSet.getLong(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.set(result, l);
                    }
                }
                case ReflectUtil.PRIMITIVE_SHORT ->
                {
                    short s = resultSet.getShort(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.set(result, s);
                    }
                }
                case ReflectUtil.PRIMITIVE_BYTE ->
                {
                    byte b = resultSet.getByte(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.set(result, b);
                    }
                }
                case ReflectUtil.PRIMITIVE_CHAR ->
                {
                    String string = resultSet.getString(columnIndex);
                    if (string != null && !string.isEmpty())
                    {
                        accessor.set(result, string.charAt(0));
                    }
                }
                case ReflectUtil.CLASS_INT ->
                {
                    int i = resultSet.getInt(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.setReference(result, i);
                    }
                }
                case ReflectUtil.CLASS_BOOL ->
                {
                    boolean b = resultSet.getBoolean(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.setReference(result, b);
                    }
                }
                case ReflectUtil.CLASS_FLOAT ->
                {
                    float f = resultSet.getFloat(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.setReference(result, f);
                    }
                }
                case ReflectUtil.CLASS_DOUBLE ->
                {
                    double d = resultSet.getDouble(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.setReference(result, d);
                    }
                }
                case ReflectUtil.CLASS_LONG ->
                {
                    long l = resultSet.getLong(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.setReference(result, l);
                    }
                }
                case ReflectUtil.CLASS_SHORT ->
                {
                    short s = resultSet.getShort(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.setReference(result, s);
                    }
                }
                case ReflectUtil.CLASS_BYTE ->
                {
                    byte b = resultSet.getByte(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.setReference(result, b);
                    }
                }
                case ReflectUtil.CLASS_CHAR ->
                {
                    String string = resultSet.getString(columnIndex);
                    if (string != null && !string.isEmpty())
                    {
                        accessor.setReference(result, string.charAt(0));
                    }
                }
                case ReflectUtil.CLASS_STRING ->
                {
                    String str = resultSet.getString(columnIndex);
                    if (str != null)
                    {
                        accessor.setReference(result, str);
                    }
                }
                case ReflectUtil.CLASS_ENUM ->
                {
                    String enumName = resultSet.getString(columnIndex);
                    if (enumName != null)
                    {
                        accessor.setReference(result, Enum.valueOf(ckazz, enumName));
                    }
                }
                case ReflectUtil.CLASS_BLOB ->
                {
                    Blob blob = resultSet.getBlob(columnIndex);
                    if (blob != null)
                    {
                        accessor.setReference(result, blob);
                    }
                }
                case ReflectUtil.CLASS_CLOB ->
                {
                    Clob clob = resultSet.getClob(columnIndex);
                    if (clob != null)
                    {
                        accessor.setReference(result, clob);
                    }
                }
                case ReflectUtil.CLASS_TIMESTAMP ->
                {
                    Timestamp l = resultSet.getTimestamp(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.setReference(result, l);
                    }
                }
                case ReflectUtil.CLASS_BIGDECIMAL ->
                {
                    String string = resultSet.getString(columnIndex);
                    if (!resultSet.wasNull())
                    {
                        accessor.setReference(result, new BigDecimal(string));
                    }
                }
                case ReflectUtil.PRIMITIVE_BYTE_ARRAY ->
                {
                    Blob blob = resultSet.getBlob(columnIndex);
                    if (blob != null)
                    {
                        byte[] array = blob.getBytes(1, (int) blob.length());
                        blob.free();
                        accessor.setReference(result, array);
                    }
                }
                case ReflectUtil.CLASS_CALENDAR ->
                {
                    Timestamp timestamp = resultSet.getTimestamp(columnIndex);
                    if (timestamp != null)
                    {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(timestamp.getTime());
                        accessor.setReference(result, calendar);
                    }
                }
                case ReflectUtil.CLASS_DATE ->
                {
                    Timestamp timestamp = resultSet.getTimestamp(columnIndex);
                    if (timestamp != null)
                    {
                        accessor.setReference(result, new Date(timestamp.getTime()));
                    }
                }
                case ReflectUtil.CLASS_TIME -> accessor.setReference(result, resultSet.getTime(columnIndex));
                case ReflectUtil.CLASS_SQL_DATE -> accessor.setReference(result, resultSet.getDate(columnIndex));
                default -> throw new IllegalArgumentException("不能默认获取值的类型:{}" + accessor.getField());
            }
        }
    }
}
