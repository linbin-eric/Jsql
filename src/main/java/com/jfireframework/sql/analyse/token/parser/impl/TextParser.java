package com.jfireframework.sql.analyse.token.parser.impl;

import java.util.Deque;
import com.jfireframework.sql.analyse.token.Token;
import com.jfireframework.sql.analyse.token.TokenType;
import com.jfireframework.sql.analyse.token.parser.TokenParser;

public class TextParser extends TokenParser
{
	
	@Override
	public int parse(String sql, int offset, Deque<Token> tokens)
	{
		if (getChar(offset, sql) != '\'')
		{
			return next.parse(sql, offset, tokens);
		}
		int index = offset;
		offset += 1;
		int length = sql.length();
		while (offset < length && getChar(offset, sql) != '\'')
		{
			offset++;
		}
		String text = sql.substring(index, offset + 1);
		tokens.push(new Token(text, TokenType.TEXT));
		return offset + 1;
	}
	
}
