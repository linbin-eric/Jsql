package com.jfireframework.sql.resultsettransfer;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer.CustomTransfer;
import com.jfireframework.sql.resultsettransfer.impl.BeanTransfer;
import com.jfireframework.sql.resultsettransfer.impl.BooleanTransfer;
import com.jfireframework.sql.resultsettransfer.impl.DoubleTransfer;
import com.jfireframework.sql.resultsettransfer.impl.EnumNameTransfer;
import com.jfireframework.sql.resultsettransfer.impl.FloatTransfer;
import com.jfireframework.sql.resultsettransfer.impl.IntegerTransfer;
import com.jfireframework.sql.resultsettransfer.impl.LongTransfer;
import com.jfireframework.sql.resultsettransfer.impl.ShortTransfer;
import com.jfireframework.sql.resultsettransfer.impl.SqlDateTransfer;
import com.jfireframework.sql.resultsettransfer.impl.StringTransfer;
import com.jfireframework.sql.resultsettransfer.impl.TimeStampTransfer;
import com.jfireframework.sql.resultsettransfer.impl.TimeTransfer;
import com.jfireframework.sql.resultsettransfer.impl.UtilDateTransfer;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public interface ResultsetTransferStore
{
    /**
     * 为一个方法登记一个ResultSetTransfer.
     * 
     * @param method
     * @return 返回该transfer的编号
     */
    int registerTransfer(Method method, JdbcTypeDictionary jdbcTypeDictionary);
    
    /**
     * 使用编号查找对应的ResultSetTransfer
     * 
     * @param sn
     * @return
     */
    ResultSetTransfer get(int sn);
    
    public static class DefaultResultSetTransferStore implements ResultsetTransferStore
    {
        
        private static final ConcurrentMap<Class<?>, Class<? extends ResultSetTransfer>> transfers = new ConcurrentHashMap<Class<?>, Class<? extends ResultSetTransfer>>();
        static
        {
            transfers.putIfAbsent(boolean.class, BooleanTransfer.class);
            transfers.putIfAbsent(Boolean.class, BooleanTransfer.class);
            transfers.putIfAbsent(double.class, DoubleTransfer.class);
            transfers.putIfAbsent(Double.class, DoubleTransfer.class);
            transfers.putIfAbsent(float.class, FloatTransfer.class);
            transfers.putIfAbsent(Float.class, FloatTransfer.class);
            transfers.putIfAbsent(int.class, IntegerTransfer.class);
            transfers.putIfAbsent(Integer.class, IntegerTransfer.class);
            transfers.putIfAbsent(long.class, LongTransfer.class);
            transfers.putIfAbsent(Long.class, LongTransfer.class);
            transfers.putIfAbsent(short.class, ShortTransfer.class);
            transfers.putIfAbsent(Short.class, ShortTransfer.class);
            transfers.putIfAbsent(Date.class, SqlDateTransfer.class);
            transfers.putIfAbsent(java.util.Date.class, UtilDateTransfer.class);
            transfers.putIfAbsent(String.class, StringTransfer.class);
            transfers.putIfAbsent(Timestamp.class, TimeStampTransfer.class);
            transfers.putIfAbsent(Time.class, TimeTransfer.class);
        }
        
        private List<ResultSetTransfer> list  = new ArrayList<ResultSetTransfer>();
        private int                     index = 0;
        
        @Override
        public int registerTransfer(Method method, JdbcTypeDictionary jdbcTypeDictionary)
        {
            ResultSetTransfer resultSetTransfer;
            Class<? extends ResultSetTransfer> type;
            Class<?> returnType;
            if (List.class.isAssignableFrom(method.getReturnType()))
            {
                returnType = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
            }
            else
            {
                returnType = method.getReturnType();
            }
            if (method.isAnnotationPresent(CustomTransfer.class))
            {
                type = method.getAnnotation(CustomTransfer.class).value();
            }
            else
            {
                type = transfers.get(returnType);
            }
            if (type != null)
            {
                try
                {
                    resultSetTransfer = type.newInstance();
                }
                catch (Exception e)
                {
                    throw new JustThrowException(e);
                }
            }
            else if (method.getReturnType().isEnum())
            {
                resultSetTransfer = new EnumNameTransfer();
            }
            else
            {
                resultSetTransfer = new BeanTransfer();
            }
            resultSetTransfer.initialize(returnType, jdbcTypeDictionary);
            list.add(resultSetTransfer);
            int sn = index;
            index += 1;
            return sn;
        }
        
        @Override
        public ResultSetTransfer get(int sn)
        {
            return list.get(sn);
        }
        
    }
}
