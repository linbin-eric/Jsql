package com.jfireframework.sql.analyse.template;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.analyse.exception.IllegalFormatException;
import com.jfireframework.sql.analyse.template.execution.Execution;
import com.jfireframework.sql.analyse.template.execution.impl.StringExecution;
import com.jfireframework.sql.analyse.template.parser.Invoker;
import com.jfireframework.sql.analyse.template.parser.TemplateParser;
import com.jfireframework.sql.analyse.template.parser.impl.CollectionParser;
import com.jfireframework.sql.analyse.template.parser.impl.ElseParser;
import com.jfireframework.sql.analyse.template.parser.impl.EndBraceParser;
import com.jfireframework.sql.analyse.template.parser.impl.ExecutionBeginParser;
import com.jfireframework.sql.analyse.template.parser.impl.ExecutionEndParser;
import com.jfireframework.sql.analyse.template.parser.impl.ExpressionParser;
import com.jfireframework.sql.analyse.template.parser.impl.ForEachParser;
import com.jfireframework.sql.analyse.template.parser.impl.IfParser;
import com.jfireframework.sql.analyse.template.parser.impl.LiteralsParser;
import com.jfireframework.sql.analyse.template.parser.impl.TemplateCharactersParser;

public class Template
{
	private static final ThreadLocal<StringCache>	LOCAL		= new ThreadLocal<StringCache>() {
																	@Override
																	protected StringCache initialValue()
																	{
																		return new StringCache();
																	};
																};
	private Deque<Execution>						executions	= new LinkedList<Execution>();
	private Execution[]								runtimeExecutions;
	private ScanMode								mode		= ScanMode.LITERALS;
	private Invoker									head		= DEFAULT_HEAD;
	private static final Invoker					DEFAULT_HEAD;
	static
	{
		TemplateParser[] parsers = new TemplateParser[] { //
		        new ExecutionBeginParser(), //
		        new ExecutionEndParser(), //
		        new IfParser(), //
		        new ElseParser(), //
		        new ForEachParser(), //
		        new EndBraceParser(), //
		        new ExpressionParser(), //
		        new TemplateCharactersParser(), //
		        new CollectionParser(), //
		        new LiteralsParser(), //
		
		};
		Invoker pred = new Invoker() {
			
			@Override
			public int scan(String sentence, int offset, Deque<Execution> executions, Template template, StringCache cache)
			{
				return offset;
			}
		};
		for (int i = parsers.length - 1; i > -1; i--)
		{
			final TemplateParser parser = parsers[i];
			final Invoker next = pred;
			Invoker invoker = new Invoker() {
				
				@Override
				public int scan(String sentence, int offset, Deque<Execution> executions, Template template, StringCache cache)
				{
					return parser.parse(sentence, offset, executions, template, cache, next);
				}
			};
			pred = invoker;
		}
		DEFAULT_HEAD = pred;
	}
	
	public ScanMode getMode()
	{
		return mode;
	}
	
	public void setMode(ScanMode mode)
	{
		this.mode = mode;
	}
	
	private Template(String sentence)
	{
		StringCache cache = new StringCache();
		int offset = 0;
		int length = sentence.length();
		mode = ScanMode.LITERALS;
		while (offset < length)
		{
			int result = head.scan(sentence, offset, executions, this, cache);
			if (result == offset)
			{
				throw new IllegalFormatException("没有解析器可以识别", sentence.substring(0, offset));
			}
			offset = result;
		}
		if (cache.count() != 0)
		{
			Execution execution = new StringExecution(cache.toString());
			executions.push(execution);
		}
		Deque<Execution> array = new LinkedList<Execution>();
		while (executions.isEmpty() == false)
		{
			array.push(executions.pollFirst());
		}
		runtimeExecutions = array.toArray(new Execution[0]);
		executions = null;
		mode = null;
	}
	
	/**
	 * 解析模板文本，返回解析后的sql语句。并且填充参数到对应的params中
	 * 
	 * @param variables
	 * @param params
	 * @return
	 */
	public String render(Map<String, Object> variables, List<Object> params)
	{
		StringCache cache = LOCAL.get();
		for (Execution execution : runtimeExecutions)
		{
			execution.execute(variables, cache, params);
		}
		String result = cache.toString();
		cache.clear();
		return result;
	}
	
	public static Template parse(String sentence)
	{
		return new Template(sentence);
	}
}
