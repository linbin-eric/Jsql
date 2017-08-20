package com.jfireframework.sql.resultsettransfer;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import com.jfireframework.sql.resultsettransfer.impl.BooleanTransfer;
import com.jfireframework.sql.resultsettransfer.impl.DoubleTransfer;
import com.jfireframework.sql.resultsettransfer.impl.FloatTransfer;
import com.jfireframework.sql.resultsettransfer.impl.IntegerTransfer;
import com.jfireframework.sql.resultsettransfer.impl.LongTransfer;
import com.jfireframework.sql.resultsettransfer.impl.ShortTransfer;
import com.jfireframework.sql.resultsettransfer.impl.SqlDateTransfer;
import com.jfireframework.sql.resultsettransfer.impl.StringTransfer;
import com.jfireframework.sql.resultsettransfer.impl.TimeStampTransfer;
import com.jfireframework.sql.resultsettransfer.impl.TimeTransfer;
import com.jfireframework.sql.resultsettransfer.impl.UtilDateTransfer;

public interface ResultSetTransferDictionary
{
    /**
     * 获取对应的结果转换处理器
     * 
     * @param type
     * @return
     */
    Class<? extends ResultSetTransfer> dictionary(Class<?> type);
    
    public class BuildInResultSetTransferDictionary implements ResultSetTransferDictionary
    {
        
        private Map<Class<?>, Class<? extends ResultSetTransfer>> transfers = new HashMap<Class<?>, Class<? extends ResultSetTransfer>>();
        
        public BuildInResultSetTransferDictionary()
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
        
        @Override
        public Class<? extends ResultSetTransfer> dictionary(Class<?> type)
        {
            return transfers.get(type);
        }
        
    }
}
