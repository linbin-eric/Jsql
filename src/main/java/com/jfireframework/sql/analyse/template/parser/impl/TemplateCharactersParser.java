package com.jfireframework.sql.analyse.template.parser.impl;

import java.util.Deque;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.jfireel.lexer.Expression;
import com.jfireframework.sql.analyse.exception.IllegalFormatException;
import com.jfireframework.sql.analyse.template.ScanMode;
import com.jfireframework.sql.analyse.template.Template;
import com.jfireframework.sql.analyse.template.execution.Execution;
import com.jfireframework.sql.analyse.template.execution.impl.TemplateCharactersExecution;
import com.jfireframework.sql.analyse.template.parser.Invoker;
import com.jfireframework.sql.analyse.template.parser.TemplateParser;

public class TemplateCharactersParser extends TemplateParser
{
	@Override
	public int parse(String sentence, int offset, Deque<Execution> executions, Template template, StringCache cache, Invoker next)
	{
		if (template.getMode() != ScanMode.LITERALS)
		{
			return next.scan(sentence, offset, executions, template, cache);
		}
		if (getChar(offset, sentence) != '#' || getChar(offset + 1, sentence) != '{')
		{
			return next.scan(sentence, offset, executions, template, cache);
		}
		extractLiterals(cache, executions);
		offset += 2;
		int start = offset;
		int length = sentence.length();
		while (getChar(offset, sentence) != '}' && offset < length)
		{
			offset++;
		}
		if (offset >= length)
		{
			throw new IllegalFormatException("语法错误，不是闭合的表达式", sentence.substring(0, start));
		}
		Execution execution = new TemplateCharactersExecution(Expression.parse(sentence.substring(start, offset)));
		executions.push(execution);
		return offset + 1;
	}
	
}
