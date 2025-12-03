package cc.jfire.jsql.executor.impl;

import cc.jfire.baseutil.reflect.ReflectUtil;
import cc.jfire.jsql.dialect.Dialect;
import cc.jfire.jsql.exception.NotSingleResultException;
import cc.jfire.jsql.executor.SqlExecutor;
import cc.jfire.jsql.metadata.TableEntityInfo;
import cc.jfire.jsql.transfer.CustomTransfer;
import cc.jfire.jsql.transfer.ResultSetTransfer;
import cc.jfire.jsql.transfer.impl.*;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class FinalExecuteSqlExecutor implements SqlExecutor
{
    @Override
    public int update(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        try (PreparedStatement prepareStatement = connection.prepareStatement(sql))
        {
            dialect.fillStatement(prepareStatement, params);
            return prepareStatement.executeUpdate();
        }
    }

    @Override
    public String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect, TableEntityInfo.ColumnInfo pkInfo) throws SQLException
    {
        switch (dialect.product())
        {
            case MYSQL, H2 ->
            {
                try (PreparedStatement prepareStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
                {
                    dialect.fillStatement(prepareStatement, params);
                    prepareStatement.executeUpdate();
                    try (ResultSet generatedKeys = prepareStatement.getGeneratedKeys())
                    {
                        ResultSetMetaData metaData = generatedKeys.getMetaData();
                        if (metaData.getColumnCount() == 1)
                        {
                            return generatedKeys.next() ? generatedKeys.getString(1) : null;
                        }
                        else
                        {
                            return generatedKeys.next() ? generatedKeys.getString(pkInfo.columnName()) : null;
                        }
                    }
                }
            }
            case DUCKDB ->
            {
                try (PreparedStatement prepareStatement = connection.prepareStatement(sql + " RETURNING " + pkInfo.columnName()))
                {
                    dialect.fillStatement(prepareStatement, params);
                    try (ResultSet resultSet = prepareStatement.executeQuery())
                    {
                        if (resultSet.next())
                        {
                            return resultSet.getString(1);
                        }
                        else
                        {
                            throw new IllegalStateException();
                        }
                    }
                }
            }
            default -> throw new UnsupportedOperationException("插入数据返回主键不支持的数据库类型：" + dialect.product());
        }
    }

    @Override
    public List<Object> queryList(String sql, ResultSetTransfer transfer, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        try (PreparedStatement prepareStatement = connection.prepareStatement(sql))
        {
            dialect.fillStatement(prepareStatement, params);
            try (ResultSet resultSet = prepareStatement.executeQuery())
            {
                List<Object> list = new LinkedList<>();
                while (resultSet.next())
                {
                    list.add(transfer.transfer(resultSet));
                }
                return list;
            }
        }
    }

    ResultSetTransfer getTransfer(AnnotatedElement annotatedElement)
    {
        Class                              itemType = null;
        Class<? extends ResultSetTransfer> transferClass;
        if (annotatedElement instanceof Class<?> c)
        {
            itemType = c;
        }
        else if (annotatedElement instanceof Method m)
        {
            if (m.getReturnType() == List.class)
            {
                itemType = (Class) ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0];
            }
            else
            {
                itemType = m.getReturnType();
            }
        }
        else
        {
            throw new IllegalArgumentException("入参必须是Class或者Method对象，当前是：" + annotatedElement.toString());
        }
        if (annotatedElement.isAnnotationPresent(CustomTransfer.class))
        {
            transferClass = annotatedElement.getAnnotation(CustomTransfer.class).value();
        }
        else
        {
            itemType = ReflectUtil.getBoxedTypeOrOrigin(itemType);
            if (itemType == Boolean.class)
            {
                transferClass = BooleanTransfer.class;
            }
            else if (itemType == Double.class)
            {
                transferClass = DoubleTransfer.class;
            }
            else if (itemType == Float.class)
            {
                transferClass = FloatTransfer.class;
            }
            else if (itemType == Integer.class)
            {
                transferClass = IntegerTransfer.class;
            }
            else if (itemType == Long.class)
            {
                transferClass = LongTransfer.class;
            }
            else if (itemType == Short.class)
            {
                transferClass = ShortTransfer.class;
            }
            else if (itemType == Date.class)
            {
                transferClass = SqlDateTransfer.class;
            }
            else if (itemType == java.util.Date.class)
            {
                transferClass = UtilDateTransfer.class;
            }
            else if (itemType == String.class)
            {
                transferClass = StringTransfer.class;
            }
            else if (itemType == Timestamp.class)
            {
                transferClass = TimeStampTransfer.class;
            }
            else if (itemType == Time.class)
            {
                transferClass = TimeTransfer.class;
            }
            else if (Enum.class.isAssignableFrom(itemType))
            {
                transferClass = EnumNameTransfer.class;
            }
            else if (itemType == Calendar.class)
            {
                transferClass = CalendarTransfer.class;
            }
            else if (itemType == byte[].class)
            {
                transferClass = ByteArrayTransfer.class;
            }
            else if (itemType == Clob.class)
            {
                transferClass = ClobTransfer.class;
            }
            else if (itemType == LocalDate.class)
            {
                transferClass = LocalDateTransfer.class;
            }
            else if (itemType == LocalDateTime.class)
            {
                transferClass = LocalDateTimeTransfer.class;
            }
            else
            {
                transferClass = BeanTransfer.class;
            }
        }
        try
        {
            ResultSetTransfer resultSetTransfer = transferClass.getDeclaredConstructor().newInstance();
            resultSetTransfer.awareType(itemType);
            return resultSetTransfer;
        }
        catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object queryOne(String sql, ResultSetTransfer transfer, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        try (PreparedStatement prepareStatement = connection.prepareStatement(sql))
        {
            dialect.fillStatement(prepareStatement, params);
            try (ResultSet executeQuery = prepareStatement.executeQuery())
            {
                if (!executeQuery.next())
                {
                    return null;
                }
                Object result = transfer.transfer(executeQuery);
                if (!executeQuery.next())
                {
                    return result;
                }
                else
                {
                    throw new NotSingleResultException();
                }
            }
        }
    }

    @Override
    public int order()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setNext(SqlExecutor next)
    {
    }
}
