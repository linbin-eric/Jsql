package com.jfireframework.sql.transfer.resultset;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import com.jfireframework.sql.transfer.resultset.impl.BooleanTransfer;
import com.jfireframework.sql.transfer.resultset.impl.DoubleTransfer;
import com.jfireframework.sql.transfer.resultset.impl.FloatTransfer;
import com.jfireframework.sql.transfer.resultset.impl.IntegerTransfer;
import com.jfireframework.sql.transfer.resultset.impl.LongTransfer;
import com.jfireframework.sql.transfer.resultset.impl.ShortTransfer;
import com.jfireframework.sql.transfer.resultset.impl.SqlDateTransfer;
import com.jfireframework.sql.transfer.resultset.impl.StringTransfer;
import com.jfireframework.sql.transfer.resultset.impl.TimeStampTransfer;
import com.jfireframework.sql.transfer.resultset.impl.TimeTransfer;
import com.jfireframework.sql.transfer.resultset.impl.UtilDateTransfer;

public interface ResultSetTransferDictionary
{
	/**
	 * 获取对应的结果转换处理器
	 * 
	 * @param <T>
	 * 
	 * @param type
	 * @return
	 */
	<T> Class<ResultSetTransfer<T>> dictionary(Class<T> type);
	
	public class BuildInResultSetTransferDictionary implements ResultSetTransferDictionary
	{
		
		private Map<Class<?>, Class<? extends ResultSetTransfer<?>>> transfers = new HashMap<Class<?>, Class<? extends ResultSetTransfer<?>>>();
		
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
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> Class<ResultSetTransfer<T>> dictionary(Class<T> type)
		{
			return (Class<ResultSetTransfer<T>>) transfers.get(type);
		}
		
	}
}
