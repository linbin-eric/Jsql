package com.jfireframework.sql.analyse.template.execution.impl;

import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.jfireel.lexer.Expression;
import com.jfireframework.sql.analyse.template.execution.Execution;

public class TemplateCharactersExecution implements Execution
{
	private Expression expression;
	
	public TemplateCharactersExecution(Expression expression)
	{
		this.expression = expression;
	}
	
	@Override
	public boolean execute(Map<String, Object> variables, StringCache cache, List<Object> params)
	{
		cache.append(expression.calculate(variables));
		return true;
	}
	
	@Override
	public void check()
	{
		// TODO Auto-generated method stub
		
	}
}
