package com.jfireframework.sql.analyse.template.parser.impl;

import java.util.Deque;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.analyse.template.ScanMode;
import com.jfireframework.sql.analyse.template.Template;
import com.jfireframework.sql.analyse.template.execution.Execution;
import com.jfireframework.sql.analyse.template.parser.Invoker;
import com.jfireframework.sql.analyse.template.parser.TemplateParser;

public class LiteralsParser extends TemplateParser
{
	
	@Override
	public int parse(String sentence, int offset, Deque<Execution> executions, Template template, StringCache cache, Invoker next)
	{
		if (template.getMode() != ScanMode.LITERALS)
		{
			offset = skipWhiteSpace(offset, sentence);
			return offset;
		}
		cache.append(getChar(offset, sentence));
		return offset + 1;
	}
	
}
