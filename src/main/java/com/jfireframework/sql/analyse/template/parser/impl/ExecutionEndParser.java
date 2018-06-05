package com.jfireframework.sql.analyse.template.parser.impl;

import java.util.Deque;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.analyse.template.ScanMode;
import com.jfireframework.sql.analyse.template.Template;
import com.jfireframework.sql.analyse.template.execution.Execution;
import com.jfireframework.sql.analyse.template.parser.Invoker;
import com.jfireframework.sql.analyse.template.parser.TemplateParser;

public class ExecutionEndParser extends TemplateParser
{
	
	@Override
	public int parse(String sentence, int offset, Deque<Execution> executions, Template template, StringCache cache, Invoker next)
	{
		if (template.getMode() != ScanMode.EXECUTION //
		        || '%' != getChar(offset, sentence) //
		        || '>' != getChar(offset + 1, sentence))
		{
			return next.scan(sentence, offset, executions, template, cache);
		}
		template.setMode(ScanMode.LITERALS);
		offset += 2;
		return offset;
	}
	
}
