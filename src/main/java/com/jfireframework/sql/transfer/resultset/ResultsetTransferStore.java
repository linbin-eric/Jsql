package com.jfireframework.sql.transfer.resultset;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.transfer.resultset.impl.BeanTransfer;
import com.jfireframework.sql.transfer.resultset.impl.EnumNameTransfer;

public class ResultsetTransferStore
{
	
	private ResultSetTransferDictionary	resultSetTransferDictionary;
	private List<ResultSetTransfer<?>>	list	= new ArrayList<ResultSetTransfer<?>>();
	private int							index	= 0;
	private SessionfactoryConfig		config;
	
	public ResultsetTransferStore(ResultSetTransferDictionary resultSetTransferDictionary, SessionfactoryConfig config)
	{
		this.resultSetTransferDictionary = resultSetTransferDictionary;
		this.config = config;
	}
	
	/**
	 * 为一个方法登记一个ResultSetTransfer.
	 * 
	 * @param method
	 * @return 返回该transfer的编号
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int registerTransfer(Method method)
	{
		ResultSetTransfer resultSetTransfer;
		Class<? extends ResultSetTransfer<?>> type;
		Class<?> returnType;
		if (List.class.isAssignableFrom(method.getReturnType()))
		{
			returnType = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
		}
		else
		{
			returnType = method.getReturnType();
		}
		if (method.isAnnotationPresent(UserDefinedTransfer.class))
		{
			type = method.getAnnotation(UserDefinedTransfer.class).value();
		}
		else
		{
			type = resultSetTransferDictionary.dictionary(returnType);
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
			resultSetTransfer = new BeanTransfer<Object>();
		}
		resultSetTransfer.initialize(returnType, config);
		list.add(resultSetTransfer);
		int sn = index;
		index += 1;
		return sn;
	}
	
	/**
	 * 使用编号查找对应的ResultSetTransfer
	 * 
	 * @param sn
	 * @return
	 */
	public ResultSetTransfer<?> get(int sn)
	{
		return list.get(sn);
	}
	
}
