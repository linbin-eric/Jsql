package com.jfirer.jsql.executor.impl;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.exception.NotSingleResultException;
import com.jfirer.jsql.executor.SqlExecutor;
import com.jfirer.jsql.transfer.CustomTransfer;
import com.jfirer.jsql.transfer.ResultSetTransfer;
import com.jfirer.jsql.transfer.impl.*;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FinalExecuteSqlExecutor implements SqlExecutor
{
    record ClassKey(String sql, Class<?> ckass)
    {
    }

    record MethodKey(String sql, Method method)
    {
    }

    ConcurrentMap<ClassKey, ResultSetTransfer>  classMap  = new ConcurrentHashMap<>();
    ConcurrentMap<MethodKey, ResultSetTransfer> methodMap = new ConcurrentHashMap<>();

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
    public String insertWithReturnKey(String sql, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        try (PreparedStatement prepareStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            dialect.fillStatement(prepareStatement, params);
            prepareStatement.executeUpdate();
            try (ResultSet generatedKeys = prepareStatement.getGeneratedKeys())
            {
                return generatedKeys.next() ? generatedKeys.getString(1) : null;
            }
        }
    }

    @Override
    public List<Object> queryList(String sql, AnnotatedElement element, List<Object> params, Connection connection, Dialect dialect) throws SQLException
    {
        try (PreparedStatement prepareStatement = connection.prepareStatement(sql))
        {
            dialect.fillStatement(prepareStatement, params);
            try (ResultSet resultSet = prepareStatement.executeQuery())
            {
                List<Object> list = new LinkedList<>();
                ResultSetTransfer resultSetTransfer = element instanceof Method ?//
                        methodMap.computeIfAbsent(new MethodKey(sql, (Method) element), methodKey -> getTransfer(methodKey.method))//
                        : classMap.computeIfAbsent(new ClassKey(sql, (Class<?>) element), classKey -> getTransfer(classKey.ckass));
                while (resultSet.next())
                {
                    list.add(resultSetTransfer.transfer(resultSet));
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
    public Object queryOne(String sql, AnnotatedElement element, List<Object> params, Connection connection, Dialect dialect) throws SQLException
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
                ResultSetTransfer resultSetTransfer = element instanceof Method ?//
                        methodMap.computeIfAbsent(new MethodKey(sql, (Method) element), methodKey -> getTransfer(methodKey.method))//
                        : classMap.computeIfAbsent(new ClassKey(sql, (Class<?>) element), classKey -> getTransfer(classKey.ckass));
                Object result = resultSetTransfer.transfer(executeQuery);
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
