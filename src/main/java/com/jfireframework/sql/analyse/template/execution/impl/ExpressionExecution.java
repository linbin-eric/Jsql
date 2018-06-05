package com.jfireframework.sql.analyse.template.execution.impl;

import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.jfireel.lexer.Expression;
import com.jfireframework.sql.analyse.template.execution.Execution;

public class ExpressionExecution implements Execution
{
	private Expression expression;
	
	public ExpressionExecution(Expression expression)
	{
		this.expression = expression;
	}
	
	@Override
	public boolean execute(Map<String, Object> variables, StringCache cache, List<Object> params)
	{
		cache.append("?");
		params.add(expression.calculate(variables));
		return true;
	}
	
	@Override
	public void check()
	{
		// TODO Auto-generated method stub
		
	}
	
}
