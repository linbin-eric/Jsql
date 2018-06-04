package com.jfireframework.sql.analyse.token;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.analyse.token.parser.TokenParser;
import com.jfireframework.sql.analyse.token.parser.impl.TemplateCharacterParser;
import com.jfireframework.sql.analyse.token.parser.impl.TextParser;
import com.jfireframework.sql.analyse.token.parser.impl.AutoCollectionParser;
import com.jfireframework.sql.analyse.token.parser.impl.ExecutionParser;
import com.jfireframework.sql.analyse.token.parser.impl.ExpressionParser;
import com.jfireframework.sql.analyse.token.parser.impl.LiteralsParser;
import com.jfireframework.sql.analyse.token.parser.impl.NumberParser;
import com.jfireframework.sql.analyse.token.parser.impl.SkipWhiteSpaceParser;
import com.jfireframework.sql.analyse.token.parser.impl.SymbolParser;

public class SqlLexer
{
	private static TokenParser firstParser;
	static
	{
		TokenParser[] parsers = new TokenParser[] { //
		        new SkipWhiteSpaceParser(), //
		        new ExecutionParser(), //
		        new ExpressionParser(), //
		        new TemplateCharacterParser(), //
		        new AutoCollectionParser(), //
		        new NumberParser(), //
		        new SymbolParser(), //
		        new TextParser(), //
		        new LiteralsParser(), //
		};
		TokenParser next = parsers[parsers.length - 1];
		next.setNextParser(new TokenParser.EmptyParser());
		for (int i = parsers.length - 2; i > -1; i--)
		{
			parsers[i].setNextParser(next);
			next = parsers[i];
		}
		firstParser = parsers[0];
	}
	private Deque<Token>	tokens	= new LinkedList<Token>();
	private Token[]			runtimeTokens;
	
	public SqlLexer(String sql)
	{
		int length = sql.length();
		int offset = 0;
		while (offset < length)
		{
			int pred = offset;
			offset = firstParser.parse(sql, offset, tokens);
			if (pred == offset)
			{
				throw new UnsupportedOperationException("无法解析");
			}
		}
		Collections.reverse((List<?>) tokens);
		runtimeTokens = tokens.toArray(new Token[tokens.size()]);
	}
	
	public String format()
	{
		StringCache cache = new StringCache();
		for (Token token : runtimeTokens)
		{
			cache.append(token.content()).append(' ');
		}
		if (cache.count() != 0)
		{
			cache.deleteLast();
		}
		return cache.toString();
	}
}
