package com.jfireframework.sql.analyse.token;

public class Token
{
	private final TokenType	tokenType;
	private final String	literals;
	
	public Token(KeyWord keyWord)
	{
		tokenType = TokenType.KEYWORD;
		literals = keyWord.name().toUpperCase();
	}
	
	public Token(Symbol symbol)
	{
		tokenType = TokenType.SYMBOL;
		literals = symbol.literals();
	}
	
	public Token(String literals)
	{
		this.literals = literals;
		tokenType = TokenType.IDENTIFIER;
	}
	
	public Token(String literals, TokenType tokenType)
	{
		this.literals = literals;
		this.tokenType = tokenType;
	}
	
	public TokenType getTokenType()
	{
		return tokenType;
	}
	
	public String content()
	{
		return literals;
	}
	
}
