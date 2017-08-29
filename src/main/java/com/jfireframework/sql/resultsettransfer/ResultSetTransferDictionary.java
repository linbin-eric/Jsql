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
            transfers.put(boolean.class, BooleanTransfer.class);
            transfers.put(Boolean.class, BooleanTransfer.class);
            transfers.put(double.class, DoubleTransfer.class);
            transfers.put(Double.class, DoubleTransfer.class);
            transfers.put(float.class, FloatTransfer.class);
            transfers.put(Float.class, FloatTransfer.class);
            transfers.put(int.class, IntegerTransfer.class);
            transfers.put(Integer.class, IntegerTransfer.class);
            transfers.put(long.class, LongTransfer.class);
            transfers.put(Long.class, LongTransfer.class);
            transfers.put(short.class, ShortTransfer.class);
            transfers.put(Short.class, ShortTransfer.class);
            transfers.put(Date.class, SqlDateTransfer.class);
            transfers.put(java.util.Date.class, UtilDateTransfer.class);
            transfers.put(String.class, StringTransfer.class);
            transfers.put(Timestamp.class, TimeStampTransfer.class);
            transfers.put(Time.class, TimeTransfer.class);
        }
        
        @Override
        public Class<? extends ResultSetTransfer> dictionary(Class<?> type)
        {
            return transfers.get(type);
        }
        
    }
}
