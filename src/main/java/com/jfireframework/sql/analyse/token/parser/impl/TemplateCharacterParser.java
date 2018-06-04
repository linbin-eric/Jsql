package com.jfireframework.sql.analyse.token.parser.impl;

import java.util.Deque;
import com.jfireframework.sql.analyse.exception.IllegalFormatException;
import com.jfireframework.sql.analyse.token.Token;
import com.jfireframework.sql.analyse.token.TokenType;
import com.jfireframework.sql.analyse.token.parser.TokenParser;

public class TemplateCharacterParser extends TokenParser
{
	
	@Override
	public int parse(String sql, int offset, Deque<Token> tokens)
	{
		if (getChar(offset, sql) != '#' || getChar(offset + 1, sql) != '{')
		{
			return next.parse(sql, offset, tokens);
		}
		int length = sql.length();
		int index = offset;
		while (offset < length && getChar(offset, sql) != '}')
		{
			offset++;
		}
		if (offset > length)
		{
			throw new IllegalFormatException("表达式没有被}结束", sql.substring(index));
		}
		tokens.push(new Token(sql.substring(index, offset + 1), TokenType.TEMPLATE_CHARACTERS));
		return offset + 1;
	}
	
}
