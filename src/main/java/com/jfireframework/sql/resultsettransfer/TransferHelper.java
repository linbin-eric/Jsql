package com.jfireframework.sql.resultsettransfer;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class TransferHelper
{
    public static String buildInitStr(Class<?> type, boolean dynamic)
    {
        if (type == Integer.class || type == int.class)
        {
            return "new com.jfireframework.sql.resultsettransfer.IntegerTransfer()";
        }
        else if (type == Short.class || type == short.class)
        {
            return "new com.jfireframework.sql.resultsettransfer.ShortTransfer()";
        }
        else if (type == Long.class || type == long.class)
        {
            return "new com.jfireframework.sql.resultsettransfer.LongTransfer()";
        }
        else if (type == Float.class || type == float.class)
        {
            return "new com.jfireframework.sql.resultsettransfer.FloatTransfer()";
        }
        else if (type == Double.class || type == double.class)
        {
            return "new com.jfireframework.sql.resultsettransfer.DoubleTransfer()";
        }
        else if (type == Boolean.class || type == boolean.class)
        {
            return "new com.jfireframework.sql.resultsettransfer.BooleanTransfer()";
        }
        else if (type == String.class)
        {
            return "new com.jfireframework.sql.resultsettransfer.StringTransfer()";
        }
        else if (type == Date.class)
        {
            return "new com.jfireframework.sql.resultsettransfer.SqlDateTransfer()";
        }
        else if (type == java.util.Date.class)
        {
            return "new com.jfireframework.sql.resultsettransfer.UtilDateTransfer()";
        }
        else if (type == Time.class)
        {
            return "new com.jfireframework.sql.resultsettransfer.TimeTransfer()";
        }
        else if (type == Timestamp.class)
        {
            return "new com.jfireframework.sql.resultsettransfer.TimeStampTransfer()";
        }
        else if (Enum.class.isAssignableFrom(type))
        {
            return "new com.jfireframework.sql.resultsettransfer.EnumTransfer(" + type.getName() + ".class)";
        }
        else
        {
            if (dynamic)
            {
                return "new com.jfireframework.sql.resultsettransfer.DynamicBeanTransfer(" + type.getName() + ".class)";
            }
            else
                return "new com.jfireframework.sql.resultsettransfer.FixBeanTransfer(" + type.getName() + ".class)";
        }
    }
}
