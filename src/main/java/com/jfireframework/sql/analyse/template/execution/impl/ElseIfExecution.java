package com.jfireframework.sql.analyse.template.execution.impl;

import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.jfireel.lexer.Expression;
import com.jfireframework.sql.analyse.template.execution.Execution;
import com.jfireframework.sql.analyse.template.execution.WithBodyExecution;

public class ElseIfExecution implements WithBodyExecution
{
	private Expression	expression;
	private Execution[]	body;
	
	public ElseIfExecution(Expression expression)
	{
		this.expression = expression;
	}
	
	@Override
	public boolean execute(Map<String, Object> variables, StringCache cache, List<Object> params)
	{
		if (expression.calculate(variables))
		{
			for (Execution each : body)
			{
				each.execute(variables, cache, params);
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public void check()
	{
	}
	
	@Override
	public void setBody(Execution... executions)
	{
		body = executions;
	}
	
	@Override
	public boolean isBodyNotSet()
	{
		return body == null;
	}
}
