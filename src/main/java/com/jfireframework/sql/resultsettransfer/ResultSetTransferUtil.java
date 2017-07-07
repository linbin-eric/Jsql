package com.jfireframework.sql.resultsettransfer;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.jfireframework.sql.resultsettransfer.impl.BeanTransfer;
import com.jfireframework.sql.resultsettransfer.impl.BooleanTransfer;
import com.jfireframework.sql.resultsettransfer.impl.DoubleTransfer;
import com.jfireframework.sql.resultsettransfer.impl.EnumTransfer;
import com.jfireframework.sql.resultsettransfer.impl.FloatTransfer;
import com.jfireframework.sql.resultsettransfer.impl.IntegerTransfer;
import com.jfireframework.sql.resultsettransfer.impl.LongTransfer;
import com.jfireframework.sql.resultsettransfer.impl.ShortTransfer;
import com.jfireframework.sql.resultsettransfer.impl.SqlDateTransfer;
import com.jfireframework.sql.resultsettransfer.impl.StringTransfer;
import com.jfireframework.sql.resultsettransfer.impl.TimeStampTransfer;
import com.jfireframework.sql.resultsettransfer.impl.TimeTransfer;
import com.jfireframework.sql.resultsettransfer.impl.UtilDateTransfer;

public class ResultSetTransferUtil
{
    private static final ConcurrentMap<Class<?>, Class<? extends ResultSetTransfer<?>>> transfers = new ConcurrentHashMap<Class<?>, Class<? extends ResultSetTransfer<?>>>();
    static
    {
        registerTransfer(boolean.class, BooleanTransfer.class);
        registerTransfer(Boolean.class, BooleanTransfer.class);
        registerTransfer(double.class, DoubleTransfer.class);
        registerTransfer(Double.class, DoubleTransfer.class);
        registerTransfer(float.class, FloatTransfer.class);
        registerTransfer(Float.class, FloatTransfer.class);
        registerTransfer(int.class, IntegerTransfer.class);
        registerTransfer(Integer.class, IntegerTransfer.class);
        registerTransfer(long.class, LongTransfer.class);
        registerTransfer(Long.class, LongTransfer.class);
        registerTransfer(short.class, ShortTransfer.class);
        registerTransfer(Short.class, ShortTransfer.class);
        registerTransfer(Date.class, SqlDateTransfer.class);
        registerTransfer(java.util.Date.class, UtilDateTransfer.class);
        registerTransfer(String.class, StringTransfer.class);
        registerTransfer(Timestamp.class, TimeStampTransfer.class);
        registerTransfer(Time.class, TimeTransfer.class);
    }
    
    public static void registerTransfer(Class<?> type, Class<? extends ResultSetTransfer<?>> transferCkass)
    {
        transfers.putIfAbsent(type, transferCkass);
    }
    
    @SuppressWarnings({ "rawtypes" })
    public static Class<? extends ResultSetTransfer> get(Class<?> type)
    {
        Class<? extends ResultSetTransfer> transfer = transfers.get(type);
        if (transfer != null)
        {
            return transfer;
        }
        if (type.isEnum())
        {
            return EnumTransfer.class;
        }
        else
        {
            return (Class<? extends ResultSetTransfer>) BeanTransfer.class;
        }
    }
}
